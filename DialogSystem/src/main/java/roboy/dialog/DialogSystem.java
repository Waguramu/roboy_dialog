package roboy.dialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import roboy.dialog.action.Action;
import roboy.dialog.action.ShutDownAction;
import roboy.dialog.personality.CuriousPersonality;
import roboy.dialog.personality.DefaultPersonality;
import roboy.dialog.personality.KnockKnockPersonality;
import roboy.dialog.personality.Personality;
import roboy.dialog.personality.SmallTalkPersonality;
import roboy.io.BingInput;
import roboy.io.BingOutput;
import roboy.io.CommandLineInput;
import roboy.io.CommandLineOutput;
import roboy.io.CerevoiceOutput;
import roboy.io.InputDevice;
import roboy.io.OutputDevice;
import roboy.linguistics.sentenceanalysis.Analyzer;
import roboy.linguistics.sentenceanalysis.DictionaryBasedSentenceTypeDetector;
import roboy.linguistics.sentenceanalysis.Interpretation;
import roboy.linguistics.sentenceanalysis.OntologyNERAnalyzer;
import roboy.linguistics.sentenceanalysis.OpenNLPPPOSTagger;
import roboy.linguistics.sentenceanalysis.OpenNLPParser;
import roboy.linguistics.sentenceanalysis.SentenceAnalyzer;
import roboy.linguistics.sentenceanalysis.SimpleTokenizer;
import roboy.talk.Verbalizer;

import roboy.memory.RoboyMind;
import edu.wpi.rail.jrosbridge.Ros;

public class DialogSystem {

	private static Ros start_rosbridge()
	{
		Ros ros = new Ros("localhost");
	    ros.connect();
	    System.out.println("ROS bridge is set up");	
	    return ros;	
	}
	
	public static void main(String[] args) throws JsonIOException, IOException, InterruptedException {

//		Ros ros = start_rosbridge();
		// Personality p = new DefaultPersonality();
//		Personality p = new CuriousPersonality();
//		Personality p = new KnockKnochPersonality();
		Personality p = new SmallTalkPersonality(new Verbalizer());
		
		InputDevice input = new CommandLineInput();
		// InputDevice input = new BingInput(rosbridge());
//		OutputDevice output = new CerevoiceOutput(ros);
		// OutputDevice output = new BingOutput();
		OutputDevice output = new CommandLineOutput();
		List<Analyzer> analyzers = new ArrayList<Analyzer>();
		analyzers.add(new SimpleTokenizer());
		analyzers.add(new OpenNLPPPOSTagger());
		analyzers.add(new DictionaryBasedSentenceTypeDetector());
		analyzers.add(new SentenceAnalyzer());
		analyzers.add(new OpenNLPParser());
		analyzers.add(new OntologyNERAnalyzer());
		
		String raw; //  = input.listen();
		Interpretation interpretation; // = analyzer.analyze(raw);
		List<Action> actions =  p.answer(new Interpretation(""));
		while(actions.isEmpty() || !(actions.get(0) instanceof ShutDownAction)){
			output.act(actions);
			raw = input.listen();
			interpretation = new Interpretation(raw);
			for(Analyzer a : analyzers){
				interpretation = a.analyze(interpretation);
			}
			actions = p.answer(interpretation);
		}
		List<Action> lastwords = ((ShutDownAction)actions.get(0)).getLastWords();
		output.act(lastwords);
	}

}
