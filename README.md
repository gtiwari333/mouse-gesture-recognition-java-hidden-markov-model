## Java Mouse Gesture Recognition using Hidden Markov Model / Vector Quantization

Draw a gesture with the mouse; location, angle and velocity features of the stroke are vector quantized
against a codebook and scored by one discrete left-to-right HMM per gesture.

#### Main classes

- `com.gt.UI.MainApp` : GUI to record training samples, train, and recognize / verify gestures
- `com.gt.gesture.proxy.OperationMediator` : codebook generation, HMM training and recognition

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
