package com.gt.gesture.proxy;

import com.gt.gesture.features.RawFeature;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * end-to-end regression test: recognise the bundled trainData recordings with the committed
 * models/codeBook and models/HMM. Needs the project root as working directory.
 * <p>
 * These are the training recordings, so this only guards against regressions (3-fold cross
 * validation on held-out recordings is ~94%). Retrain whenever the feature extraction changes.
 */
@Tag( "integration" )
class OperationMediatorRecognitionTest {

	private static final double MIN_ACCURACY = 0.9;

	private static RawFeature read( File f ) throws Exception {
		try ( ObjectInputStream in = new ObjectInputStream( new FileInputStream( f ) ) ) {
			return ( RawFeature ) in.readObject( );
		}
	}

	@Test
	void everyGestureHasAModel( ) {
		OperationMediator m = new OperationMediator( );
		assertArrayEquals( m.readTrainData1D( ), m.readRegGestureModels( ) );
	}

	@Test
	void recognisesTrainingRecordings( ) throws Exception {
		File root = new File( "trainData" );
		assertTrue( root.isDirectory( ), "run from the project root" );

		OperationMediator m = new OperationMediator( );
		int total = 0, correct = 0;
		List< String > misses = new ArrayList<>( );
		File[] gestures = root.listFiles( File::isDirectory );
		Arrays.sort( gestures );
		for ( File g : gestures ) {
			for ( File f : g.listFiles( ( d, n ) -> n.endsWith( ".TRAINDATA" ) ) ) {
				String recognised = m.recognizeGesture( read( f ) );
				total++;
				if ( recognised.equals( g.getName( ) ) ) {
					correct++;
				} else {
					misses.add( g.getName( ) + " -> " + recognised );
				}
			}
		}
		assertTrue( ( double ) correct / total >= MIN_ACCURACY, correct + "/" + total + " correct, misses: " + misses );
	}

	@Test
	void verifyComparesIgnoringCase( ) throws Exception {
		File f = new File( "trainData/LeftOnly" ).listFiles( ( d, n ) -> n.endsWith( ".TRAINDATA" ) )[ 0 ];
		OperationMediator m = new OperationMediator( );
		assertEquals( m.recognizeGesture( read( f ) ).equalsIgnoreCase( "leftonly" ), m.verify( "leftonly", read( f ) ) );
	}
}
