Loopier {
	// boot server and display meter and scope with 2 channels
	*boot { arg scopeStyle = 2, server = Server.default;
		/*server.makeWindow;*/
		// server.boot;
		server.waitForBoot {
			server.meter.window.bounds_(Rect(0,1024-400,138,253));
			server.scope(2).style_(scopeStyle)
			.window.bounds_(Rect( 0, 1024, 200, 250)); // s.meter.width
			// FreqScope.new(400, 200, 0, server: server);
			server.plotTree;
		};
	}

	*livecode{ arg name = "Loopier", venue = "nowhere";
		var doc;

		name = name++"@"++venue++"-"++Date.getDate.dayStamp;
		name.postln;

		doc = Document.current.(name);
		doc.keyDownAction_({|doc, char, mod, unicode, keycode |
			var linenr, string;
			// evaluating code (the next line will use .isAlt, when that is available
			if((mod.isAlt) && ((keycode==124)||(keycode==123)||(keycode==125)||(keycode==126)), { // alt + left or up or right or down arrow keys
				linenr = doc.string[..doc.selectionStart-1].split($\n).size;
				doc.selectLine(linenr);
				string = doc.selectedString;
				if(keycode==123, { // not 124, 125,
					// this.freeAgent(string);
					"yep".postln;
				}, {
					// this.opInterpreter(string);
					"nope".postln;
				});
			});
		});
/*		thisProcess.interpreter.preProcessor = {|code|
			"---------".postln;
			try { code } { "oops".postln };
			code.postln;
			"---------".postln;
			// thisProcess.interpreter.interpret(code);
		};*/
	}

	*quit { arg server = Server.default;
		server.quit;
	}

	//
	*livecodingInit { arg setuppath = "livecoding/setup.scd";
		if (setuppath == nil ,{
			"*** ERROR *** You need to provide a path to the setup.scd file".postln;
		},{
		(setuppath).loadRelative;
	});
	}

	// loads samples from a folder.  Samples must be in subfolders within the given path
	// returns a Dictionary with name:buffer pairs.  To get samples directly from folder see loadSampleFiles below.
	*loadSampleDirectories { arg path, s = Server.default;
		var d = Dictionary.new;
		d.add(\foldernames -> PathName(path).entries);
		d[\foldernames].do({
			arg item, i;
			var tempdict = item.entries;
			tempdict.do({ arg sf, i; tempdict.put(i, Buffer.read(s, sf.fullPath)) });
			d.add(item.folderName -> tempdict);
			item.folderName.post; "(".post; item.entries.size.post; ")".postln;
		});

		d.removeAt(\foldernames);
		/*d[\foldernames].do {
			arg item, i;
			// i.post;": ".post;d[\foldernames][i].folderName.post;": ".post;item.folderName.postln;
			d.add(item.folderName -> item.entries.do({
				arg sf, i;
				var buf = Buffer.read(s, sf.fullPath);
				i.post; "...".post; sf.parent.name.postln;
			}));
			item.folderName.post; "(".post; item.files.size.post; ")".postln;
		};*/

		^d;
	}

	*dictionary { arg dict;
		dict[\foldernames].do({
			arg item, i;
			item.folderName.post; "(".post; item.entries.size.post; ")".postln;
		});
	}

	// loads samples from a folder.  Samples must be files within the given path
	// returns a Dictionary
	*loadSampleFiles { arg path = "samples/", s = Server.default;
		var d = ();
		PathName(path).entries.do({ |item, i|
			// item.fullPath.postln;
			d.put(i, Buffer.read(s, item.fullPath));
		});
		^d;
	}

	// loads samples from a folder.  Samples must be files within the given path
	// returns an Array
	*loadSamplesArray { arg path = "samples/", s = Server.default;
		var a = Array.new;
		PathName(path).entries.do({ |item, i|
			// item.fullPath.postln;
			a = a.add(Buffer.read(s, item.fullPath));
		});
		^a;
	}


	// loads samples from a folder.
	// returns a Dictionary
	*loadSamples { arg path = "samples/", s = Server.default;
		// var d = loadSampleDirectories(path, s); -- TODO: check if it has subfolders
		var d = loadSampleFiles(path, s);
		^d;
	}

	*allocBuffers  { arg numOfBuffers = 4,
		server = Server.default,
		seconds = 4,
		channels = 1;
		var buffers = ();
		numOfBuffers.do({|i| buffers.put(i, Buffer.alloc(server, server.sampleRate * seconds, channels))});
		^buffers;
	}

	// Return a list of all compiled SynthDef names
	*synthDefList {
		var names = SortedList.new;

		SynthDescLib.global.synthDescs.do { |desc|
			if(desc.def.notNil) {
				// Skip names that start with "system_"
				if ("^[^system_]".matchRegexp(desc.name)) {
					names.add(desc.name);
				};
			};
		};

		^names;
	}

	// Prints a list of all compiled Synthdef names
	*listSynthDefs {
		Loopier.synthDefList.do {|i|
			i.postln;
		}
	}

	// Resets the FM7 modulators in the Pbindef
	*resetFM7mods { arg name;
		6.do { |i|
			6.do { |n|
				Pbindef(name.asSymbol,
					((\mod++i++n).asSymbol), 0
				);
			};
		};
	}

	// Returns an integer from a given array of 0s and 1s
	// @param  array   Array[0,1]     An array representing a binary number
	//                                              WANRING!: Reads from right to left!
	//                                              e.g: [0,1] = 1; [1,0] = 2
	*binaryArrayAsInteger{ arg array;
		// (x + 2^0) + (x + 2^1) + (x + 2^2) + .. + (x + 2^n)
		// where x=(value[0||1]) and n=(digit position in binary form)
		^(array * 2.pow((0..array.size-1)).reverse).sum;
	}
}
