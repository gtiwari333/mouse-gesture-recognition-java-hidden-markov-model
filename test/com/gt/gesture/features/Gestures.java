package com.gt.gesture.features;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * synthetic mouse gestures for the tests
 */
public final class Gestures {

	private Gestures( ) {
	}

	/** a polyline through the given corners, one point every {@code step} pixels, 10 ms apart */
	public static RawFeature polyline( int step, int[]... corners ) {
		List< Point > pts = new ArrayList<>( );
		for ( int c = 0; c + 1 < corners.length; c++ ) {
			int[] a = corners[ c ], b = corners[ c + 1 ];
			int n = ( int ) Math.max( 1, Math.round( Math.hypot( b[ 0 ] - a[ 0 ], b[ 1 ] - a[ 1 ] ) / step ) );
			for ( int i = 0; i < n; i++ ) {
				pts.add( new Point( a[ 0 ] + ( b[ 0 ] - a[ 0 ] ) * i / n, a[ 1 ] + ( b[ 1 ] - a[ 1 ] ) * i / n ) );
			}
		}
		int[] last = corners[ corners.length - 1 ];
		pts.add( new Point( last[ 0 ], last[ 1 ] ) );
		double[] time = new double[ pts.size( ) ];
		for ( int i = 0; i < time.length; i++ ) {
			time[ i ] = 1_000_000 + 10.0 * i;
		}
		return new RawFeature( time, pts.toArray( new Point[ 0 ] ) );
	}
}
