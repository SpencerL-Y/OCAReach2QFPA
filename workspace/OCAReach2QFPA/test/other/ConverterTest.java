package other;

import automata.counter.OCA;
import automata.counter.OCAOp;
import ocareach.Converter;
import parser.OCDGenerator;

public class ConverterTest {
	public static void main(String[] args) {
		// type1 example 
		OCA oca = new OCA();
		for(int i = 0; i < 5; i++) {
			oca.addState(i);
		}
		oca.addTransition(0, OCAOp.Sub, 1);
		oca.addTransition(1, OCAOp.Add, 4);
		oca.addTransition(4, OCAOp.Sub, 0);
		oca.addTransition(1, OCAOp.Add, 2);
		oca.addTransition(2, OCAOp.Sub, 2);
		oca.addTransition(2, OCAOp.Add, 3);
		oca.setInitIndex(0);
		oca.setTargetIndex(3);
		
		//random example
		OCDGenerator ocdg = new OCDGenerator();
		oca = ocdg.genRandomOca(5);
		
		
		// type12 example
		
		oca = new OCA();
		for(int i = 0; i < 6;i ++) {
			oca.addState(i);
		}
		oca.addTransition(0, OCAOp.Sub, 1);
		oca.addTransition(1, OCAOp.Sub, 2);
		oca.addTransition(2, OCAOp.Add, 2);
		oca.addTransition(2, OCAOp.Add, 3);
		oca.addTransition(3, OCAOp.Add, 4);
		oca.addTransition(4, OCAOp.Add, 3);
		oca.addTransition(1, OCAOp.Sub, 5);
		oca.addTransition(5, OCAOp.Add, 0);
		oca.setInitIndex(0);
		oca.setTargetIndex(3);
		
		
		//trivial example
		/*//trivial 1
		oca = new OCA();
		oca.addState(0);
		oca.addTransition(0, OCAOp.Add, 0);
		oca.setInitIndex(0);
		oca.setTargetIndex(0);
		*/
		
		/*
		oca = new OCA();
		oca.addState(0);
		oca.addTransition(0, OCAOp.Sub, 0);
		oca.setInitIndex(0);
		oca.setTargetIndex(0);
		*/
		
		/*// trivial scc
		  
		  oca = new OCA();
		  for(int i = 0; i < 4; i ++) {
			  oca.addState(i);
		  }
		  oca.addTransition(0, OCAOp.Add, 1);
		  oca.addTransition(1, OCAOp.Sub, 2);
		  oca.addTransition(2, OCAOp.Sub, 3);
		*/
		
		
		oca = new OCA();
		for(int i = 0; i < 8; i++) {
			oca.addState(i);
		}
		oca.addTransition(0, OCAOp.Sub, 1);
		oca.addTransition(7, OCAOp.Sub, 1);
		oca.addTransition(2, OCAOp.Sub, 6);
		oca.addTransition(6, OCAOp.Sub, 2);
		oca.addTransition(1, OCAOp.Add, 2);
		oca.addTransition(1, OCAOp.Add, 7);
		oca.addTransition(3, OCAOp.Add, 3);
		oca.addTransition(4, OCAOp.Add, 5);
		oca.addTransition(5, OCAOp.Add, 4);
		oca.setInitIndex(0);
		oca.setTargetIndex(4);
		 		
		
		Converter con = new Converter(oca);
		String result = con.convert();
		System.out.println(result);
		
	}
}