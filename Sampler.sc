Sampler{
	classvar <samples;
	var <pbind;
	var <>samplename;

	*new { arg name = "apbind", samplefoldername;
		^super.new.init(name, samplefoldername);
	}

	*initClass {
		StartUp.add {
			/*

			bplay: basic stereo buffer player
			This is the bread and butter of my SynthDef library
			Designed for simple repeated playback of stereo buffers inside of a pattern

			out = channel out
			amp = volume
			pan = stereo panning of sample
			buf = buffer index
			rate = rate of playback (or pitch)
			prate = pitched rated (diatonic)
			start = position of playback (usually not used)
			attack
			sutain = duration of the sample
			release

			*/
			SynthDef(\playbuf, { |out=0, amp=0.5, pan=0, buf, rate=1, t_trig=1, start=0, attack=0.01, sustain=1, release=0.01|
				var sig, end, startpos, isForward, frames;
				isForward = (rate < 0);
				frames = BufFrames.kr(buf);
				startpos = abs( (frames * isForward) - (frames * start) ) - (2 * isForward);
				sig = PlayBuf.ar(2, buf, BufRateScale.kr(buf) * rate, 1, startpos);
				sig = sig * EnvGen.kr(Env.linen(attack, sustain, release), t_trig, doneAction:2);
				sig = Pan2.ar(sig, pan);
				sig = sig * amp;
				Out.ar(out, sig);
			}).add;

			/*

			bplaym: basic mono buffer player
			Designed for simple repeated playback of mono buffers inside of a pattern

			out = channel out
			amp = volume
			pan = stereo panning of sample
			buf = buffer index
			rate = rate of playback (or pitch)
			prate = pitched rated (diatonic)
			start = position of playback (usually not used)
			attack
			sutain = duration of the sample
			release

			*/
			SynthDef(\playbufm, { |out=0, amp=0.5, pan=0, buf, rate=1, t_trig=1, start=0, attack=0.01, sustain=1, release=0.01|
				var sig, end, startpos, isForward, frames;
				isForward = (rate < 0);
				frames = BufFrames.kr(buf);
				startpos = abs( (frames * isForward) - (frames * start) ) - (2 * isForward);
				sig = PlayBuf.ar(1, buf, BufRateScale.kr(buf) * rate, 1, startpos);
				sig = sig * EnvGen.kr(Env.linen(attack, sustain, release), t_trig, doneAction:2);
				sig = sig * amp;
				sig = Pan2.ar(sig, pan);
				Out.ar(out, sig);
			}).add;
		}

	}


	*loadSamples { arg path, s = Server.default;
		if ( path == nil,
			{path = thisProcess.nowExecutingPath.dirname +/+ "samples/"}
		);
		samples = Dictionary.new;
		samples.add(\foldernames -> PathName(path).entries);
		samples[\foldernames].do {
			arg item, i;
			// i.post;": ".post;samples[\foldernames][i].folderName.post;": ".post;item.folderName.postln;
			samples.add(item.folderName -> item.entries.do({
				arg sf;
				if ((sf.extension == "wav") || (sf.extension == "flac")
					|| (sf.extension == "aiff") || (sf.extension == "aif"))
				{
					i.post;": ".post;sf.folderName.post;"/".post;sf.fileName.postln;
					Buffer.read(s, sf.fullPath);
				}
			}));
		};
		^samples;
	}

	*listSamples {
		if (samples == nil,
			{^"No samples loaded"},
			{
				samples.keys.do {
					arg item, i;
					i.post;": ".post;item.postln;
				};
			}
		);
	}

	sample{ arg bufname;
		^this.pbind.getName

	}

	init{ arg name, samplefoldername;
		samplename = samplefoldername;
		// load samples
		if( samples == nil,
			{ ^"Please load samples first with Sampler.loadSamples(['path/to/samples'])" }
		);
		// take first sample if not specified
		if (samplefoldername == nil,
			{ samplefoldername = samples.keys.asArray[0] }
		);
		// init Pbind
		pbind = Pbindef(name.asSymbol, \instrument, \playbuf,
			\buf, samples[samplefoldername][0]
		).play;
		^samples[samplefoldername][0];
	}

}