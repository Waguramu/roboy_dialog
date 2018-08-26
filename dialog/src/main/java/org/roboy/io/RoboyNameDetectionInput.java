package org.roboy.io;

import java.io.IOException;

import org.roboy.linguistics.sentenceanalysis.Interpretation;
/* 
 edu.cmu.sphinx - responsible for 
 linking Sphinx functions
 the rest is necessary to 
 configure input device
*/
import edu.cmu.sphinx.api.*;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
/** 
* \brief Class detecting RoboyModel name
* \author Petr Romanov
* \version 1.0
* \date 21.04.2017
* \details Initiates native sphinx function of live speech analysis and checks the stream
*/
public class RoboyNameDetectionInput implements InputDevice{
	/**
	* 'link' to the object of Recognizer for correct stopping before 
	* deletion of the RoboyNameDetectorInput object
	*/
	protected LiveSpeechRecognizer recog_copy;
	/**
	* constructor which initialises recognition
	*/
	public RoboyNameDetectionInput() throws IOException {
		//configure input sound stream reader
		Mixer.Info[] mixerInfo;
		mixerInfo = AudioSystem.getMixerInfo();
		Line.Info targetDLInfo = new Line.Info(TargetDataLine.class);
		//uncomment next block to check the number of input port 
		//for necessary device and select it. Default port 0
		/* 
		for(int i = 0; i < mixerInfo.length; i++)
		{
			Mixer currentMixer = AudioSystem.getMixer(mixerInfo[i]);
			if( currentMixer.isLineSupported(targetDLInfo) )
				System.out.println( mixerInfo[i].getName()+'\n' );
		}
		JavaSoundAudioIO aio = new JavaSoundAudioIO();
		aio.selectMixer(1);
		*/
		// Configuration object contains all info about words to recognise: rules, sounds, dictionary
		Configuration configuration = new Configuration();
		/*Configure sphinx for 'RoboyModel' itself and phrases with it
		 1 - contains phonetic model - how all words to recognize may be pronounced
		 2 - probabilities of phrases that are expected to contain name(!not so sure!)
		 9462 - name automatically generated by online tool that prepares files for sphinx 
		 can be any other, but hard to change everywhere each time
		*/
		configuration.setDictionaryPath("src//edu//cmu//sphinx//models//9462//9462.dic");
		configuration.setLanguageModelPath("src//edu//cmu//sphinx//models//9462//9462.lm");
		//native sphinx file - NN for matching words in phrases
		configuration.setAcousticModelPath("src//edu//cmu//sphinx//models//en-us//en-us");
		//initialise native recognizer with set conf.
		LiveSpeechRecognizer recognizer = new LiveSpeechRecognizer(configuration);
		//start listening
		recognizer.startRecognition(true);
		//create external 'link' to the object of recognizer to be able to stop it correctly before deletion of the RoboyNameDetectorInput object
		recog_copy = recognizer;
	}
	/**
	* function for correct stopping recognition
	*/
	public void stopListening(){
		 recog_copy.stopRecognition();
		 recog_copy = null;
	}
	
	@Override 
	/**
	* tracks what was said
	* \return A signal that RoboyModel is one of the words in just said phrase
	*/
	public Input listen() throws InterruptedException, IOException {
		//get a string that was recognized
		String utterance = recog_copy.getResult().getHypothesis();
		if ( utterance.contains("ROBOY") ){
			Interpretation interpretation = new Interpretation();
			interpretation.setRoboy(true);
			return new Input(null, interpretation);
		} else {
			return new Input(null);
		}
	}
}
