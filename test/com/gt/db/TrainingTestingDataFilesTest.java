package com.gt.db;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class TrainingTestingDataFilesTest {

	@Test
	void pointsAtTheSameFoldersAsTheDatabase( ) {
		assertEquals( new File( "trainData" ), new TrainingTestingDataFiles( "train" ).getDataPath( ) );
		assertEquals( new File( "testData" ), new TrainingTestingDataFiles( "test" ).getDataPath( ) );
	}

	@Test
	void listsGestureFoldersAndFiles( @TempDir Path dir ) throws Exception {
		for ( String g : new String[] { "b", "a" } ) {
			Files.createDirectories( dir.resolve( g ) );
			Files.createFile( dir.resolve( g ).resolve( g + "1.TRAINDATA" ) );
		}
		Files.createFile( dir.resolve( ".DS_Store" ) );
		TrainingTestingDataFiles t = new TrainingTestingDataFiles( "train" );
		t.setDataPath( dir.toFile( ) );

		assertArrayEquals( new String[] { "a", "b" }, t.readDataFolder( ) );
		File[][] files = t.readDataFilesList( );
		assertEquals( "a1.TRAINDATA", files[ 0 ][ 0 ].getName( ) );
		assertEquals( "b1.TRAINDATA", files[ 1 ][ 0 ].getName( ) );
	}
}
