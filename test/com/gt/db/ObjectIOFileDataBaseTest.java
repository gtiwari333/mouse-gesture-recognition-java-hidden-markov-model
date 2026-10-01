package com.gt.db;

import com.gt.gesture.features.Gestures;
import com.gt.gesture.features.RawFeature;
import com.gt.hmm.HMMModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ObjectIOFileDataBaseTest {

	/** folders are otherwise hard-coded relative to the working directory */
	private static ObjectIOFileDataBase db( DBMode mode, Path dir ) {
		ObjectIOFileDataBase db = new ObjectIOFileDataBase( );
		db.setMode( mode );
		db.CURRENTFOLDER = dir.toString( );
		return db;
	}

	private static HMMModel hmm( double p ) {
		HMMModel m = new HMMModel( );
		m.setPi( new double[] { p } );
		return m;
	}

	@Test
	void hmmModelsAreListedSortedWithoutExtensionOrStrayFiles( @TempDir Path dir ) throws Exception {
		ObjectIOFileDataBase db = db( DBMode.HMM_MODEL, dir );
		db.saveModel( hmm( 1 ), "Zig.Zag" ); // a dot in the name must survive
		db.saveModel( hmm( 2 ), "Circle" );
		Files.createFile( dir.resolve( ".DS_Store" ) );
		Files.createFile( dir.resolve( "README" ) );

		assertArrayEquals( new String[] { "Circle", "Zig.Zag" }, db.getRegisteredModelNames( ) );
		assertEquals( 1.0, ( ( HMMModel ) db.readModel( "Zig.Zag" ) ).getPi( )[ 0 ] );

		Model[][] all = db.readAllDataofCurrentMode( );
		assertEquals( 2, all[ 0 ].length );
		assertEquals( 2.0, ( ( HMMModel ) all[ 0 ][ 0 ] ).getPi( )[ 0 ] );
	}

	@Test
	void missingFolderHasNoModels( @TempDir Path dir ) {
		assertEquals( 0, db( DBMode.HMM_MODEL, dir.resolve( "missing" ) ).getRegisteredModelNames( ).length );
	}

	@Test
	void trainDataIsGroupedByGestureInNameOrder( @TempDir Path dir ) throws Exception {
		ObjectIOFileDataBase db = db( DBMode.TRAINDATA, dir );
		RawFeature line = Gestures.polyline( 3, new int[] { 0, 0 }, new int[] { 60, 0 } );
		db.saveModel( line, "Right" );
		db.saveModel( line, "Down" );
		Thread.sleep( 2 ); // file names are time stamped
		db.saveModel( line, "Down" );
		Files.createFile( dir.resolve( ".DS_Store" ) );

		String[] names = db.getRegisteredModelNames( );
		Model[][] data = db.readAllDataofCurrentMode( );
		assertArrayEquals( new String[] { "Down", "Right" }, names );
		assertEquals( 2, data[ 0 ].length, "data[i] must belong to names[i]" );
		assertEquals( 1, data[ 1 ].length );
		assertEquals( line, data[ 1 ][ 0 ] );
	}
}
