package com.gt.gesture.features;

import org.junit.jupiter.api.Test;

import java.awt.Point;

import static org.junit.jupiter.api.Assertions.*;

class RawFeatureTest {

	@Test
	void cloneIsEqualButIndependent( ) throws Exception {
		RawFeature rf = new RawFeature( new double[] { 1, 2 }, new Point[] { new Point( 1, 1 ), new Point( 2, 2 ) } );
		RawFeature copy = ( RawFeature ) rf.clone( );
		assertEquals( rf, copy );
		assertEquals( rf.hashCode( ), copy.hashCode( ) );

		copy.getCurTime( )[ 0 ] = 99;
		copy.getDrawPoint( )[ 0 ].x = 99;
		assertEquals( 1, rf.getCurTime( )[ 0 ] );
		assertEquals( 1, rf.getDrawPoint( )[ 0 ].x, "points are deep copied" );
	}
}
