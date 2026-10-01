package com.gt.db;

import com.gt.hmm.HMMModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ObjectIOTest {

	@Test
	void roundTripCreatesParentFolders( @TempDir Path dir ) {
		HMMModel m = new HMMModel( );
		m.setPi( new double[] { 1, 0 } );
		m.setTransition( new double[][] { { 0.5, 0.5 }, { 0, 1 } } );

		String file = dir.resolve( "a/b/model.HMM_MODEL" ).toString( );
		ObjectIO< HMMModel > io = new ObjectIO<>( );
		io.setModel( m );
		io.saveModel( file );

		HMMModel read = new ObjectIO< HMMModel >( ).readModel( file );
		assertArrayEquals( m.getPi( ), read.getPi( ) );
		assertArrayEquals( m.getTransition( ), read.getTransition( ) );
	}

	@Test
	void missingFileReadsAsNull( @TempDir Path dir ) {
		assertNull( new ObjectIO< HMMModel >( ).readModel( dir.resolve( "nope" ).toString( ) ) );
	}
}
