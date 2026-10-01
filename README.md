## Java Mouse Gesture Recognition using Hidden Markov Model / Vector Quantization

Draw a gesture with the mouse. Location, angle and velocity features of the stroke are vector quantized
against a codebook, and the resulting symbol sequence is scored by one discrete left-to-right HMM per
gesture. The gesture whose HMM gives the highest Viterbi score wins.

#### Main classes

- `com.gt.UI.MainApp` : GUI to record training samples, train, and recognize / verify gestures
- `com.gt.gesture.proxy.OperationMediator` : codebook generation, HMM training and recognition

## Algorithm

```
 mouse drag: points (x, y) + time stamps (ms)          DataCapturePanel ─► RawFeature
   │  GestureFeatureExtractor   down-sample ─► geometry + kinematics features ─► normalise
   ▼
 T × 10 feature vectors
   │  Codebook.quantize         nearest of 64 codewords (LBG / k-means trained)
   ▼
 T symbols in 0..63
   │  HiddenMarkov              Viterbi score against each gesture's 4-state left-to-right HMM
   ▼
 best matching gesture
```

### 1. Capture (`gesture.mouseCapture.DataCapturePanel`, `gesture.features.RawFeature`)

While the mouse button is held down every drag event is recorded as a screen point (x, y) together with
`System.currentTimeMillis()`. A recording needs more than 10 points to be used. Training samples are saved
as serialized `RawFeature` objects.

### 2. Feature extraction (`gesture.features.GestureFeatureExtractor`)

Based on Yoon et al., *Hand gesture recognition using combined features of location, angle and velocity*
(reference [4] below).

1. **Down-sampling**: every 3rd captured point is kept (every point for very short gestures, fewer than
   6 points), giving points p_0..p_n with times t_0..t_n.
2. **Global geometry** of all captured points: the centre of gravity (mean point) and the bounding box
   (x_min, x_max, y_min, y_max).
3. For every pair of successive points (p_i, p_{i+1}), i = 0..n−1, one **10-dimensional feature vector**:

| # | Feature | Definition |
|---|---|---|
| 1 | location relative to CG | \|p_i − CG\|, min–max normalised over the gesture to [0, 1] |
| 2 | angle with CG | orientation of p_i − CG |
| 3 | angle with initial point | orientation of p_i − p_0 |
| 4 | angle with end point | orientation of p_i − p_n |
| 5 | velocity | \|p_{i+1} − p_i\| / (t_{i+1} − t_i), divided by the gesture's maximum velocity, so in [0, 1] (0 when the time difference is 0) |
| 6–9 | angles with the bounding box corners | orientation of p_i − (x_max, y_max), (x_max, y_min), (x_min, y_max), (x_min, y_min) |
| 10 | direction of motion | full-circle direction of p_{i+1} − p_i |

**Angles** are quantized to 10° steps:

- *orientation* (features 2–4, 6–9): ⌈atan(dy/dx)·180/π / 10⌉, in [−9, 9]; a vertical vector (dx = 0) is ±9.
  Opposite directions share the same orientation;
- *direction* (feature 10): ⌈atan2(dy, dx)·180/π / 10⌉, in [−18, 18]. This is what tells, e.g., a left
  stroke from a right stroke, because a straight stroke looks the same in both directions to every
  other feature.

Location and velocity are normalised per gesture, and all angles are relative, so the features do not
depend on where the gesture is drawn or how large or fast it is.

*Design note*: using orientation-only angles plus one direction feature was chosen by cross validation
(see *Accuracy*). Full-circle atan2 angles for every feature scored lower (≈ 91.8 %), and orientation
angles alone (≈ 94.5 %) confused `LeftOnly` with `RightOnly`.

### 3. Vector quantization (`hmm.classify.vq.Codebook`)

A 64-entry codebook is trained on the feature vectors of **all** training recordings with the
**LBG (Linde–Buzo–Gray) algorithm**:

1. start with a single codeword: the mean of all training vectors;
2. **split** every codeword c into c·(1 + ε) and c·(1 − ε), with ε = 0.01;
3. **k-means**: assign every vector to its nearest codeword (Euclidean distance), move each codeword to
   the mean of its cell, and repeat until the total distortion improves by less than 0.1 or 0.1 %
   (at most 100 iterations). An empty cell takes the nearest vector from the closest cell that has
   more than one vector;
4. repeat 2–3 until there are 64 codewords.

Quantizing a gesture replaces every feature vector by the index (0..63) of its nearest codeword. The
features are not rescaled before clustering, so the angle features (range ±9 / ±18) weigh more in the
distance than location and velocity (range 0..1).

### 4. Hidden Markov models (`hmm.HiddenMarkov`)

One **discrete HMM per gesture** with N = 4 states and M = 64 output symbols (one per codeword).

- **Topology**: left-to-right (Bakis) model; from state i only states i, i+1 and i+2 are reachable
  (a_ij = 0 for j < i or j > i + 2), and every sequence starts in the first state (π = [1, 0, 0, 0]).
- **Initialisation**: random transition and output probabilities, each row normalised to sum to 1. Every
  gesture gets a fresh model, seeded with the gesture name's hash code, so retraining on the same data
  gives the same models.
- **Training**: Baum–Welch (EM) on all recordings of the gesture, using the *scaled* forward–backward
  algorithm (Rabiner, 1989) to avoid underflow:
  - forward: α̂_t(i) is α_t(i) normalised so Σ_i α̂_t(i) = 1, with scale c_t = 1 / Σ_i α_t(i); then
    log P(O | λ) = −Σ_t log c_t;
  - backward β̂_t(i) scaled with the same c_t;
  - ξ_t(i,j) = α̂_t(i)·a_ij·b_j(O_{t+1})·β̂_{t+1}(j) and γ_t(i) = α̂_t(i)·β̂_t(i) / c_t, summed over all
    recordings;
  - a_ij = Σ ξ_t(i,j) / Σ γ_t(i) over t < T−1, and b_j(v) = Σ_{t: O_t = v} γ_t(j) / Σ_t γ_t(j);
  - every allowed probability is floored at 1e-11 and the rows are renormalised;
  - iterations stop when the total log likelihood changes by less than 1e-5 (relative), or after 50.
- **Recognition**: the **Viterbi** algorithm in the log domain gives the log probability of the best state
  path for each gesture's HMM (π_i = 0 is log 0 = −∞, so every path starts in the first state); the gesture
  with the highest score is returned. *Verify* checks that the
  recognised gesture is the expected one.

### Accuracy

Measured on the bundled `trainData/` recordings (23 gestures, 652 recordings):

| | Accuracy |
|---|---|
| Training recordings, committed models | 98.2 % (640 / 652) |
| Held-out recordings, 3-fold cross validation over 6 random splits | ≈ 96.2 % |

Gestures with fewer than 3 recordings (`NewRound`, `thhree`) are only used for training in the cross
validation; record more samples before relying on them.

### Verification against reference sources

| Component | Reference | How it was checked |
|---|---|---|
| HMM forward, Viterbi, Baum–Welch | Rabiner, *A Tutorial on Hidden Markov Models and Selected Applications in Speech Recognition*, Proc. IEEE 77(2), 1989 (eqs. 18–40, 91–110) | a one-iteration Baum–Welch update, log P(O\|λ) and the Viterbi score match an independent unscaled textbook implementation to ~1e-16; unit tests compare forward and Viterbi against brute-force enumeration of all state paths |
| VQ codebook | Linde, Buzo, Gray, *An Algorithm for Vector Quantizer Design*, IEEE Trans. Commun. 28(1), 1980 | unit tests: k-means fixed point (every codeword is the mean of its cell), cluster separation |
| Features | Yoon, Soh, Bae, Yang, *Hand gesture recognition using combined features of location, angle and velocity*, Pattern Recognition 34(7), 2001 | the same feature families (location relative to the centroid, angles, velocity, k-means codebook, left-to-right HMM); the exact feature set and quantization here are this project's own, chosen by cross validation, and are unit tested for direction, translation invariance and bounding-box geometry |

The 1e-11 probability floor is a practical addition that is not in the references (it keeps codewords
never seen in training from making a gesture impossible).

### References

1. L. R. Rabiner, *A Tutorial on Hidden Markov Models and Selected Applications in Speech Recognition*,
   Proceedings of the IEEE, 77(2), pp. 257–286, 1989.
   [PDF](https://www.biostat.wisc.edu/~kbroman/teaching/statgen/2004/refs/rabiner.pdf)
   ([alternate copy](https://cs.brown.edu/courses/csci2840/spring-2021/handouts/A%20tutorial%20on%20hidden%20markov%20models.pdf))
2. *An Erratum for "A Tutorial on Hidden Markov Models and Selected Applications in Speech Recognition"*
   (corrections to the scaling and multiple observation sequence sections).
   [PDF](https://home.engineering.iastate.edu/alexs/classes/2024_Spring_575/HW/HW5/PDFs/errata.pdf)
3. Y. Linde, A. Buzo, R. M. Gray, *An Algorithm for Vector Quantizer Design*, IEEE Transactions on
   Communications, 28(1), pp. 84–95, 1980.
   [Summary](https://en.wikipedia.org/wiki/Linde%E2%80%93Buzo%E2%80%93Gray_algorithm)
4. H.-S. Yoon, J. Soh, Y. J. Bae, H. S. Yang, *Hand gesture recognition using combined features of location,
   angle and velocity*, Pattern Recognition, 34(7), pp. 1491–1501, 2001.
   [Record](https://koasas.kaist.ac.kr/handle/10203/82715)

#### Build, test and run (Maven, JDK 17+)

- `mvn test` : unit tests + an end-to-end recognition test over `trainData/` using the committed models
- `mvn package && java -jar target/mouse-gesture-recognition-hmm-1.0-SNAPSHOT.jar` : launch the GUI
  (run from the project root, model/data paths are relative)
- Retrain after changing the feature extraction or adding samples: `OperationMediator.generateCodeBook()`
  then `OperationMediator.trainHMM()` (also available from the GUI). Training is seeded, so the same
  recordings always give the same models.

#### Folder conventions

- `trainData/<Gesture>/<Gesture><timestamp>.TRAINDATA` : recorded strokes (serialized `RawFeature`)
- `models/codeBook/codebook.CODEBOOK_MODEL` : VQ codebook (64 codewords)
- `models/HMM/<Gesture>.HMM_MODEL` : one trained HMM per gesture
