LoopierBuffer {
	classvar <> record_channel; // class variable
	var <>bufs; // instance variables

	// constructor
	*new { arg server = Server.default, num_bufs = 4, buf_length = 4;
		^super.new.init(server, num_bufs, buf_length);
	}

	/**
	* Initialize a given number of buffers
	* @param     Server  server      Server where to alloc buffers
	* @param     int     num_bufs    Number of buffers
	* @param     int     buf_length  In seconds
	* @return    Buffer[]            Array of initialized buffers
	*/
	init { arg server, num_bufs, buf_length;
		record_channel = 1;

		// just in case an impossible number is passed
		if(num_bufs < 1, {
			num_bufs = 4;
			"Warnging: Too few buffers.  Set to default 4 buffers".postln;
			"".postln;
		});

		bufs = ();
		num_bufs.do({|i|
			bufs.put(i, Buffer.alloc(server, server.sampleRate * buf_length));
		});

		num_bufs.post; " buffers have been allocated on ".post; server.postln;

		^bufs;
	}

	/**
	* Return the buffer at the given index
	* @param     int     buf_num     Index of the buffer in the 'bufs' array
	* @return    Buffer              Buffer at the given index
	*/
	at { arg buf_num;
		^bufs.at(buf_num);
	}
}