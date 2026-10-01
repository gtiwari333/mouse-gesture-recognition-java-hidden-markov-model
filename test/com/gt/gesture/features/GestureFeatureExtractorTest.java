package com.gt.gesture.features;

import org.junit.jupiter.api.Test;

import java.awt.Point;

import static org.junit.jupiter.api.Assertions.*;

class GestureFeatureExtractorTest {

	private static GestureFeature[] extract( RawFeature rf ) {
		return new GestureFeatureExtractor( rf ).getExtractedFeature( );
	}

	@Test
	void anglesAreOrientationsInMinus90To90( ) {
		// screen coordinates: y grows downwards
		assertEquals( 0, GestureFeatureExtractor.getAngleYbyX( 0, 5 ) ); // right
		assertEquals( 0, GestureFeatureExtractor.getAngleYbyX( 0, -5 ) ); // left: same orientation
		assertEquals( 9, GestureFeatureExtractor.getAngleYbyX( 5, 0 ) ); // down
		assertEquals( -9, GestureFeatureExtractor.getAngleYbyX( -5, 0 ) ); // up
		assertEquals( 0, GestureFeatureExtractor.getAngleYbyX( 0, 0 ) );
		assertEquals( 5, GestureFeatureExtractor.getAngleYbyX( 1, 1 ) ); // 45 degrees -> ceil(4.5)
		assertEquals( 5, GestureFeatureExtractor.getAngleYbyX( -1, -1 ) ); // -135 degrees has the 45 degree orientation
		assertEquals( -4, GestureFeatureExtractor.getAngleYbyX( -1, 1 ) ); // -45 degrees -> ceil(-4.5)
	}

	@Test
	void directionCoversTheFullCircle( ) {
		assertEquals( 0, GestureFeatureExtractor.getDirection( 0, 5 ) ); // right
		assertEquals( 18, GestureFeatureExtractor.getDirection( 0, -5 ) ); // left
		assertEquals( 9, GestureFeatureExtractor.getDirection( 5, 0 ) ); // down
		assertEquals( -9, GestureFeatureExtractor.getDirection( -5, 0 ) ); // up
		assertEquals( -13, GestureFeatureExtractor.getDirection( -1, -1 ) ); // -135 degrees -> ceil(-13.5)
	}

	@Test
	void oppositeDirectionsGiveDifferentFeatures( ) {
		// a straight stroke is symmetric: only the direction of motion tells left from right
		GestureFeature[] right = extract( Gestures.polyline( 3, new int[] { 10, 50 }, new int[] { 200, 50 } ) );
		GestureFeature[] left = extract( Gestures.polyline( 3, new int[] { 200, 50 }, new int[] { 10, 50 } ) );
		assertEquals( 0, right[ 0 ].getMotionDirection( ) );
		assertEquals( 18, left[ 0 ].getMotionDirection( ) );
		assertFalse( java.util.Arrays.equals( right[ 0 ].getFeatureVector( ), left[ 0 ].getFeatureVector( ) ) );
	}

	@Test
	void verticalStrokeHasVerticalAngle( ) {
		GestureFeature[] down = extract( Gestures.polyline( 3, new int[] { 50, 10 }, new int[] { 50, 200 } ) );
		assertEquals( 9, down[ down.length - 1 ].getAngleWithInitialPt( ), "dx = 0 must not collapse to angle 0" );
	}

	@Test
	void everyFeatureVectorIsFilled( ) {
		RawFeature rf = Gestures.polyline( 2, new int[] { 10, 10 }, new int[] { 150, 10 }, new int[] { 150, 150 } );
		GestureFeature[] f = extract( rf );
		int framed = rf.getCurTime( ).length / GestureFeatureExtractor.SAMPLE_PER_FRAME;
		assertEquals( framed - 1, f.length );
		// no trailing all-zero frames
		GestureFeature last = f[ f.length - 1 ];
		assertNotEquals( 0.0, last.getVelocity( ) );
		for ( GestureFeature g : f ) {
			assertEquals( 10, g.getFeatureVector( ).length );
			for ( double v : g.getFeatureVector( ) ) {
				assertTrue( Double.isFinite( v ) );
			}
			assertTrue( g.getLocationRelativeToCG( ) >= 0 && g.getLocationRelativeToCG( ) <= 1 );
			assertTrue( g.getVelocity( ) >= 0 && g.getVelocity( ) <= 1 );
		}
	}

	@Test
	void boundingBoxCornerAngles( ) {
		// L shape, every coordinate > 0: bounding box (100,100)-(300,300)
		RawFeature rf = Gestures.polyline( 3, new int[] { 100, 100 }, new int[] { 100, 300 }, new int[] { 300, 300 } );
		GestureFeature first = extract( rf )[ 0 ]; // the point (100, 100) = (xMin, yMin)
		assertEquals( 0, first.getxMinyMinAngle( ), "the point is the (xMin, yMin) corner itself" );
		assertEquals( -9, first.getxMinyMaxAngle( ), "(xMin, yMax) corner is straight below" );
		assertEquals( 0, first.getxMaxyMinAngle( ), "(xMax, yMin) corner is straight to the right" );
		assertEquals( 5, first.getxMaxyMaxAngle( ), "(xMax, yMax) corner is diagonally below right" );
	}

	@Test
	void translationInvariant( ) {
		RawFeature a = Gestures.polyline( 3, new int[] { 10, 10 }, new int[] { 100, 60 }, new int[] { 20, 120 } );
		RawFeature b = Gestures.polyline( 3, new int[] { 410, 310 }, new int[] { 500, 360 }, new int[] { 420, 420 } );
		GestureFeature[] fa = extract( a ), fb = extract( b );
		assertEquals( fa.length, fb.length );
		for ( int i = 0; i < fa.length; i++ ) {
			assertArrayEquals( fa[ i ].getFeatureVector( ), fb[ i ].getFeatureVector( ), 1e-9, "frame " + i );
		}
	}

	@Test
	void shortGestureUsesEveryPoint( ) {
		RawFeature rf = new RawFeature( new double[] { 0, 10, 20, 30 },
				new Point[] { new Point( 0, 0 ), new Point( 5, 0 ), new Point( 10, 0 ), new Point( 15, 0 ) } );
		assertEquals( 3, extract( rf ).length );
	}

	@Test
	void singlePointIsRejected( ) {
		RawFeature rf = new RawFeature( new double[] { 0 }, new Point[] { new Point( 1, 1 ) } );
		assertThrows( IllegalArgumentException.class, ( ) -> new GestureFeatureExtractor( rf ) );
	}

	@Test
	void identicalTimestampsDoNotDivideByZero( ) {
		RawFeature rf = new RawFeature( new double[ 12 ], Gestures.polyline( 5, new int[] { 0, 0 }, new int[] { 55, 0 } ).getDrawPoint( ) );
		for ( GestureFeature g : extract( rf ) ) {
			assertEquals( 0.0, g.getVelocity( ) );
		}
	}
}
