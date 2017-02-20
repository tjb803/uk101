/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2015,2016
 */
package uk101.utils;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import uk101.io.AudioEncoder;
import uk101.io.KansasCityDecoder;
import uk101.io.KansasCityEncoder;
import uk101.io.Tape;
import uk101.machine.Loudspeaker;

/**
 * Utility program to take ASCII, binary or audio input and play as 
 * Kansas City encoded audio to the speaker.
 *
 * Usage:
 *    TapePlay [options] inputfile(s) 
 *
 * where:
 *    inputfile(s): the names of the ASCII, binary or audio input files
 *
 * options:
 *    -binary: input is binary, defaults to auto-selected
 *    -sampleRate: audio sample rate (default 48kHz)
 *    -sampleSize: audio sample size (default 16 bits)
 *    -baudRate: audio baud rate (default 300)
 *    -leadIn: time to play lead-in tone (default to 5s)
 *    -leadOut: time to play lead-out tone (defaults to leadIn) 
 *    -leadGap: time between tape segments (default to 2s)
 *    -sineWave: generate pure sine wave audio tones 
 *    -inputRate: audio baud rate of input, if audio encoded (defaults to baudRate)
 *    -inputPhase: audio phase angle of input, if audio encoded (defaults to 90) 
 */
public class TapePlay {

    public static void main(String[] args) throws Exception {
        // Handle parameters
        Args.Map options = Args.optionMap();
        options.put("binary");
        options.put("sampleRate", "samplerate (8000 to 96000)");
        options.put("sampleSize", "samplesize (8 or 16)");
        options.put("baudRate", "baudrate (300, 600 or 1200)");
        options.put("leadIn", "+leadin");
        options.put("leadOut", "+leadout");
        options.put("leadGap", "segmentgap");
        options.put("sineWave");
        options.put("inputRate", "inputbaudrate (300, 600 or 1200)");
        options.put("inputPhase", "inputphaseangle (0, 90, 180 or 270)");
        Args parms = new Args(TapePlay.class, "inputfile(s)", args, options);
        int count = parms.getParameterCount();

        List<File> inputFiles = parms.getInputFiles(1, count);
        int inputFormat = parms.getFlag("binary") ? Tape.STREAM_BINARY : Tape.STREAM_SELECT;
        int sampleRate = parms.getInteger("sampleRate", AudioEncoder.RATE48K);
        int sampleSize = parms.getInteger("sampleSize", AudioEncoder.BIT16);
        int baudRate = parms.getInteger("baudRate", KansasCityEncoder.BAUD300);
        int leadIn = parms.getInteger("leadIn", 5);
        int leadOut = parms.getInteger("leadOut", leadIn);
        int leadGap = parms.getInteger("leadGap", 2);
        int inputRate = parms.getInteger("inputRate", baudRate);
        int inputPhase = parms.getInteger("inputPhase", 90);
        boolean sineWave = parms.getFlag("sineWave");

        // Check parameters
        if ((inputFiles.isEmpty()) ||
                (sampleRate < 8000 || sampleRate > 96000) ||
                (sampleSize != 8 && sampleSize != 16) ||
                (baudRate != 300 && baudRate != 600 && baudRate != 1200) ||
                (inputRate != 300 && inputRate != 600 && inputRate != 1200) ||
                (inputPhase%90 != 0)) {
            parms.usage();
        }

        // Create encoders/decoders and loudspeaker output device
        KansasCityDecoder decoder = new KansasCityDecoder(inputRate, inputPhase);
        KansasCityEncoder encoder = new KansasCityEncoder(sampleRate, sampleSize, baudRate, sineWave);
        encoder.setLeader(leadIn*1000, leadOut*1000);
        Loudspeaker speaker = new Loudspeaker(encoder);
        
        // Play the input files to the speaker
        count = 0;
        speaker.open();
        for (File inputFile : inputFiles) {
            InputStream input = Tape.getInputStream(inputFile, inputFormat, decoder);
            if (count++ > 0) {
                encoder.setLeader(leadGap*1000, leadOut*1000);
            }
            speaker.play(input);
            input.close(); 
        }    
        speaker.close();
    }
}
