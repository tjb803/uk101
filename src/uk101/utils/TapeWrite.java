/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010,2014
 */
package uk101.utils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import uk101.io.AudioEncoder;
import uk101.io.KansasCityEncoder;
import uk101.io.Stream;

/**
 * Utility program to take ASCII or binary input and write an 
 * equivalent UK101 binary or audio tape
 *
 * Usage:
 *    TapeWrite [options] inputfile outputtape
 *
 * where:
 *    inputfile: the name of the text input file
 *    outputtape: the name for the output tape
 *
 * options:
 *    -binary: create binary tape (default)
 *    -audio: create audio tape
 *    -sampleRate: audio sample rate (default 44.1kHz)
 *    -sampleSize: audio sample size (default 16 bits)
 *    -baudRate: audio baud rate (default 300)
 *    -leadIn: time to play lead-in tone (default to 5s)
 *    -leadOut: time to play lead-out tone (defaults to leadIn) 
 *
 * @author Baldwin
 */
public class TapeWrite {

    public static void main(String[] args) throws Exception {
        // Handle parameters
        Args.Map options = Args.optionMap();
        options.put("binary");
        options.put("audio");
        options.put("sampleRate", "samplerate (8000 to 48000)");
        options.put("sampleSize", "samplesize (8 or 16)");
        options.put("baudRate", "baudrate (300, 600 or 1200)");
        options.put("leadIn", "+leadin");
        options.put("leadOut", "leadout");
        Args parms = new Args("TapeWrite", "inputfile outputtape", args, options);

        File inputFile = parms.getInputFile(1);
        File outputFile = parms.getOutputFile(2);
        int outputFormat = parms.getFlag("audio") ? Stream.STREAM_AUDIO : Stream.STREAM_BINARY;
        int sampleRate = parms.getInteger("sampleRate", AudioEncoder.RATE44K);
        int sampleSize = parms.getInteger("sampleSize", AudioEncoder.BIT16);
        int baudRate = parms.getInteger("baudRate", KansasCityEncoder.BAUD300);
        int leadIn = parms.getInteger("leadIn", 5);
        int leadOut = parms.getInteger("leadOut", leadIn);

        // Check parameters
        if ((inputFile == null || outputFile == null) ||
                (sampleRate < 8000 || sampleRate > 48000) ||
                (sampleSize != 8 && sampleSize != 16) ||
                (baudRate != 300 && baudRate != 600 && baudRate != 1200)) {
            parms.usage();
        }

        // Create input/output streams
        KansasCityEncoder encoder = new KansasCityEncoder(sampleRate, sampleSize, baudRate);
        encoder.setLeader(leadIn*1000, leadOut*1000);
        InputStream input = Stream.getInputStream(inputFile, Stream.STREAM_SELECT, null); 
        OutputStream output = Stream.getOutputStream(outputFile, outputFormat, encoder);

        // Copy the input to the output
        Stream.copy(input, output);
        output.close();
        input.close();
    }
}
