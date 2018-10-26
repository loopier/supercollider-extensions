LoopLang {
	classvar globaldocnum;
	var <>doc, docnum, doccolor, oncolor, activecolor, offcolor, deadcolor, proxyspace, groups, score;
	var agentDict, instrDict, ixiInstr, effectDict, varDict, snapshotDict, scoreArray;
	var scale, tuning, chosenscale, tonic;
	var midiclient, eventtype, suicidefork;
	var langCommands, englishCommands, language, english;
	var matrixArray, initargs; // matrix vars
	var projectname, key;
	var randomseed;
	var thisversion = 3;

	*new { arg project="default", keyarg="C", txt=false, newdoc=false, language, dicts, score;
		Loopier.boot;
		^super.new.envirSetup(txt, newdoc, project);
		// ^super.new.initXiiLang( project, keyarg, txt, newdoc, language, dicts, score);
	}

	initXiiLang {arg project, keyarg, txt, newdoc, lang, dicts, score;

		this.envirSetup( txt, newdoc, project );
	}

	//set up document and the keydown control
	envirSetup { arg txt, newdoc, venue = "Nowhere", name = "Loopier";
		if(newdoc, {
			doc = Document.new;
		}, {
			doc = Document.current;
		});

		// Read object from file
		// Object.readArchive("ixilang/"++project++"/colors.ixi"

		// if(txt == false, { doc.string_("", rangesize:doc.string.size ) });
		doc.name = name++" @ "++venue;
		// doc.promptToSave_(false);
		doc.keyDownAction_({|doc, char, mod, unicode, keycode |
			var linenr, string;
			// evaluating code (the next line will use .isAlt, when that is available
			if((mod & 524288 == 524288) && ((keycode==124)||(keycode==123)||(keycode==125)||(keycode==126)), { // alt + left or up or right or down arrow keys
				linenr = doc.string[..doc.selectionStart-1].split($\n).size;
				doc.selectLine(linenr);
				string = doc.selectedString;
				if(keycode==123, { // not 124, 125,
					// this.freeAgent(string);
					"yep".postln;
				}, {
					this.opInterpreter(string);
				});
			});
		});
		doc.onClose_({
			// xxx free buffers
			ixiInstr.freeBuffers; // not good as playscore reads a new doc (NEED TO FIX)
			proxyspace.end(4);  // free all proxies
		});
	}

	opInterpreter{ arg string;
		var splits, agent, argument, value;

		"Interpreting: ".post; string.postln;
		splits = string.split($ );
		agent = splits.removeAt(0);
		argument = splits.removeAt(0);
		value = splits.at(0);
		"Agent: ".post; agent.postln;
		"Argument: ".post; argument.postln;
		"Value: ".post; value.postln;

		("Pbindef(\\"++agent++",\\"++argument++","++value++")").interpret;

/*		var oldop, operator; // the various operators of the language
		var methodFound = false;
		operator = block{|break| // better NOT mess with the order of the following... (operators using -> need to be before "->")
			langCommands.do({arg op; var suggestedop, space;
				var c = string.find(op);
				if(c.isNil.not, {
					space = string[c..string.size].find(" "); // allowing for longer operators with same beginning as shorter
					if(space.isNil.not, {
						suggestedop = string[c..(c+(space-1))];
					},{
						suggestedop = op;
					});
					if(suggestedop.size == op.size, { // this is a bit silly (longer op always has to be after the short)
						methodFound = true;
						break.value(op);
					});
				});
			});
			if(methodFound == false, {" --->   ixi lang error : OPERATOR NOT FOUND !!!".postln; });
		};


		if(english.not, { // this is the only place of non-english code apart from in the init function
			oldop = operator;
			operator = try{ language[oldop.asSymbol] } ? operator; // if operator exists in the lang dict, else use the given
			operator = operator.asString;
			string = string.replace(oldop, operator);
		});

		switch(operator)
			{"dict"}{ // TEMP: only used for debugging
				"-- Groups : ".postln;
				Post << groups; "\n".postln;

				"-- agentDicts : ".postln;
				Post << agentDict; "\n".postln;

				"-- snapshotDicts : ".postln;
				Post << snapshotDict; "\n".postln;

		//		"-- scoreArray : ".postln;
		//		Post << scoreArray; "\n".postln;

		//		"-- buffers : ".postln;
		//		Post << ixiInstr.returnBufferDict; "\n".postln;

			}*/
	}
}