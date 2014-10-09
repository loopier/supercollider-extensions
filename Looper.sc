Looper {
	var server;
	var num_buffers;
	var buffer_max_length;
	var buffers;
	var <>loopers;

	*new { arg server_in = Server.default, num_buffers_in = 4, buf_length_in = 16;
		^super.new.initLooper(server_in, num_buffers_in, buf_length_in);
	}


	initLooper { arg server_in, num_buffers_in, buf_length_in;
		server = server_in;
		num_buffers = num_buffers_in;
		buffer_max_length = buf_length_in;
		buffers = ();
		loopers = ();
		("Server: "++server.value).postln;
		("# of Buffers: "++num_buffers.value).postln;
		("Buffer length (sec): "++buffer_max_length.value).postln;

		this.gui; // if this goes after 'this.setupAudio', audio must be forked
		this.setupAudio;
	}

	setupAudio {
			num_buffers.do({|i|
				buffers.put(i, Buffer.alloc(server, server.sampleRate * buffer_max_length, 2));
				buffers[i].postln;
				loopers.put(i, Synth(\looper, [buf:i]));
			});

		// buffers.postln;
		loopers.postln;
	}

	// create a window with controls for as many loopmachines as buffers
	gui {
		var window, rec_all_button;
		window = Window.new.front;
		num_buffers.do({|i|
			this.looperGui(window, index:i, y:i*30);
		});

		rec_all_button = Button(window, Rect(0, window.bounds.height - 30, 90, 30))
		.name_("rec_all_button")
		.states_([
			["REC ALL", Color.black, Color.gray],
			["REC ALL", Color.black, Color.red]
		]).
		action_({
			rec_all_button.name.postln;
			num_buffers.do({|i|
				// !!! NEEDS LOOPER-GUI TO BE STORED IN A LIST IN ORDER TO ACCESS
				// ITS ELEMENTS
			});
		});
	}

	// control panel for one loopmachine (buffer)
	looperGui { |parent, index=0, x=0, y=0, w=30, h=30|
		var rec_button, rate_slider, rate_numbox, overdub_checkbox, clear_button, rate_spec;

		rate_spec = ControlSpec(0.1,100, \exp, 0.1, 1);

		rec_button = Button(parent, Rect(x,y,w*2,h))
		.name_("rec_button_"++index)
		.states_([
            ["REC", Color.black, Color.gray],
            ["STOP", Color.black, Color.red]
		])
		.action_({
			rec_button.name.postln;
			("Recording on buffer: "++loopers[index]).postln;
			loopers[index].set(\t_trig, 1);
			loopers[index].set(\t_mic, 1);
		});

		rate_slider = Slider(parent, Rect(x+rec_button.bounds.left+rec_button.bounds.width, y, w * 3, h))
		.name_("rate_slider_"++index)
		.orientation_(\horizontal)
		.action_({
			var value = rate_spec.map(rate_slider.value);
			// update numbox when the value has changed in the slider
			rate_numbox.value_(value);
			rate_slider.name.postln;
			// !!! add rate control
			loopers[index].set(\rate, value);
		});

		rate_numbox = NumberBox(parent,  Rect(x+rate_slider.bounds.left+rate_slider.bounds.width, y, w * 1.5, h))
		.name_("rate_numbox_"++index)
		.step_(0.1)
		.scroll_step_(0.1)
		.valueAction_(1)
		.action_({
			var value = rate_spec.map(rate_numbox.value);
			// update slider when the value has changed in the numbox
			rate_slider.value_(rate_numbox.value); // !!! FIX
			rate_numbox.name.postln;
			// !!! add rate control
			// loopers[index].set(\rate, value);
		});

		overdub_checkbox = CheckBox(parent,  Rect(x+rate_numbox.bounds.left+rate_numbox.bounds.width + 10, y, w * 3, h))
		.name_("overdub_checkbox_"++index)
		.string_("Overdub")
		.action_({overdub_checkbox.name.postln;});

		clear_button = Button(parent, Rect(x+overdub_checkbox.bounds.left+overdub_checkbox.bounds.width + 10,y,w*2,h))
		.name_("clear_button_"++index)
		.states_([
            ["CLEAR", Color.black, Color.gray]
		])
		.action_({
			clear_button.name.postln;
			loopers[index].set(\reset, 1);
			buffers[index].zero;
		});
	}

	free {
		loopers.size.do({|i| loopers[i].free});
	}
}