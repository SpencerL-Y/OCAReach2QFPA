package other;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import automata.counter.OCA;
import automata.counter.OCAOp;
import ocareach.Converter;
import parser.OCDGenerator;

public class ConverterTest {
	public static void main(String[] args) throws IOException {
		// type1 example //DONE
		OCA oca = new OCA();
		for(int i = 0; i < 5; i++) {
			oca.addState(i);
		}
		oca.addTransition(0, OCAOp.Sub, 0);
		/*
		oca.addTransition(1, OCAOp.Add, 4);
		oca.addTransition(4, OCAOp.Sub, 0);
		oca.addTransition(1, OCAOp.Add, 2);
		oca.addTransition(2, OCAOp.Sub, 2);
		oca.addTransition(2, OCAOp.Add, 3);*/
		oca.setInitIndex(0);
		oca.setTargetIndex(0);
		
		//random example
		
		OCDGenerator ocdg = new OCDGenerator();
		//for(int i = 0; i < 500; i++) {
		//oca = ocdg.genRandomOca(5);
		
		// type12 example //DONE
		/*
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
		//trivial 1 //DONE
		oca = new OCA();
		oca.addState(0);
		oca.addTransition(0, OCAOp.Add, 0);
		oca.setInitIndex(0);
		oca.setTargetIndex(0);
		
		
		//trivial 2 // DONE
		oca = new OCA();
		oca.addState(0);
		oca.addTransition(0, OCAOp.Sub, 0);
		oca.setInitIndex(0);
		oca.setTargetIndex(0);
		
		//trivial 3
		
		oca = new OCA();
		oca.addState(0);
		oca.addTransition(0, OCAOp.Add, 0);
		oca.addTransition(0, OCAOp.Sub, 0);
		oca.setInitIndex(0);
		oca.setTargetIndex(0);
		
		
		// trivial scc //DONE
		
		oca = new OCA();
		for(int i = 0; i < 4; i ++) {
			oca.addState(i);
		}
		oca.addTransition(0, OCAOp.Add, 1);
		oca.addTransition(1, OCAOp.Sub, 2);
		oca.addTransition(2, OCAOp.Sub, 3);
		oca.setInitIndex(0);
		oca.setTargetIndex(3);
		
		
		
		//type 132 //DONE
		
		oca = new OCA();
		for(int i = 0; i < 8; i++) {
			oca.addState(i);
		}
		oca.addTransition(0, OCAOp.Sub, 1);
		oca.addTransition(7, OCAOp.Sub, 0);
		oca.addTransition(2, OCAOp.Sub, 6);
		oca.addTransition(6, OCAOp.Sub, 2);
		oca.addTransition(2, OCAOp.Sub, 3);
		oca.addTransition(3, OCAOp.Sub, 4);
		oca.addTransition(1, OCAOp.Add, 2);
		oca.addTransition(1, OCAOp.Add, 7);
		oca.addTransition(3, OCAOp.Add, 3);
		oca.addTransition(4, OCAOp.Add, 5);
		oca.addTransition(5, OCAOp.Add, 4);
		oca.setInitIndex(0);
		oca.setTargetIndex(4);
		
		
		//type 13 //DONE
		
		oca = new OCA();
		for(int i = 0; i < 4; i++) {
			oca.addState(i);
		}
		oca.addTransition(0, OCAOp.Sub, 1);
		oca.addTransition(1, OCAOp.Sub, 2);
		oca.addTransition(0, OCAOp.Sub, 3);
		oca.addTransition(3, OCAOp.Sub, 0);
		oca.addTransition(1, OCAOp.Add, 1);
		oca.addTransition(2, OCAOp.Sub, 2);
		oca.setInitIndex(0);
		oca.setTargetIndex(2);
		
		
		

		
		
		//type 3 //DONE
		oca = new OCA();
		for(int i = 0; i < 2; i++) {
			oca.addState(i);
		}
		oca.addTransition(0, OCAOp.Add, 0);
		oca.addTransition(0, OCAOp.Sub, 1);
		oca.addTransition(1, OCAOp.Sub, 1);
		oca.setInitIndex(0);
		oca.setTargetIndex(1);
		
		
		//type32 DONE
		
		oca = new OCA();
		for(int i = 0; i < 5; i++) {
			oca.addState(i);
		}
		oca.addTransition(0, OCAOp.Sub, 1);
		oca.addTransition(1, OCAOp.Sub, 1);
		oca.addTransition(4, OCAOp.Sub, 0);
		oca.addTransition(0, OCAOp.Add, 3);
		oca.addTransition(3, OCAOp.Add, 4);
		oca.addTransition(1, OCAOp.Add, 2);
		oca.addTransition(2, OCAOp.Add, 2);
		oca.setInitIndex(0);
		oca.setTargetIndex(2);
		
		// other bugs
		
		oca = new OCA();
		for(int i = 0; i < 3; i++) {
			oca.addState(i);
		}
		oca.addTransition(0, OCAOp.Add, 0);
		oca.addTransition(0, OCAOp.Sub, 2);
		oca.addTransition(2, OCAOp.Sub, 1);
		oca.addTransition(1, OCAOp.Add, 0);
		oca.setInitIndex(0);
		oca.setTargetIndex(0);
		
		
		
		
		oca = new OCA();
		for(int i = 0; i < 3; i++) {
			oca.addState(i);
		}
		oca.addTransition(0, OCAOp.Sub, 0);
		oca.addTransition(0, OCAOp.Add, 1);
		oca.addTransition(1, OCAOp.Add, 1);
		oca.addTransition(1, OCAOp.Sub, 1);
		oca.addTransition(1, OCAOp.Sub, 2);
		oca.addTransition(2, OCAOp.Add, 2);
		oca.setInitIndex(0);
		oca.setTargetIndex(2);
		
		
		oca = new OCA();
		for(int i = 0; i < 3; i++) {
			oca.addState(i);
		}
		oca.addTransition(0, OCAOp.Sub, 1);
		oca.addTransition(1, OCAOp.Add, 2);
		oca.addTransition(0, OCAOp.Add, 0);
		oca.addTransition(1, OCAOp.Add, 0);
		oca.addTransition(1, OCAOp.Sub, 1);
		oca.setInitIndex(0);
		oca.setTargetIndex(2);
		
		
		oca = new OCA();
		for(int i = 0; i < 5; i++) {
			oca.addState(i);
		}
		oca.addTransition(0, OCAOp.Add, 0);
		oca.addTransition(0, OCAOp.Sub, 3);
		oca.addTransition(3, OCAOp.Sub, 3);
		oca.addTransition(3, OCAOp.Sub, 4);
		oca.addTransition(4, OCAOp.Sub, 3);
		oca.addTransition(4, OCAOp.Add, 4);
		oca.addTransition(4, OCAOp.Sub, 1);
		oca.addTransition(4, OCAOp.Sub, 2);
		oca.addTransition(2, OCAOp.Add, 2);
		oca.setInitIndex(0);
		oca.setTargetIndex(2);
		
		
		
		oca = new OCA();
		for(int i = 0; i < 8; i++) {
			oca.addState(i);
		}
		oca.addTransition(0, OCAOp.Sub, 0);
		oca.addTransition(1, OCAOp.Sub, 1);
		oca.addTransition(3, OCAOp.Sub, 3);
		oca.addTransition(0, OCAOp.Sub, 3);

		oca.addTransition(0, OCAOp.Sub, 1);
		oca.addTransition(0, OCAOp.Sub, 3);
		oca.addTransition(0, OCAOp.Sub, 4);
		oca.addTransition(0, OCAOp.Sub, 7);
		oca.addTransition(1, OCAOp.Sub, 5);
		oca.addTransition(1, OCAOp.Sub, 2);

		oca.addTransition(3, OCAOp.Sub, 6);
		oca.addTransition(3, OCAOp.Sub, 2);
		

		oca.addTransition(4, OCAOp.Sub, 1);
		oca.addTransition(5, OCAOp.Sub, 2);
		oca.addTransition(6, OCAOp.Sub, 2);
		oca.addTransition(7, OCAOp.Sub, 3);
		
		oca.setInitIndex(0);
		
		oca.setTargetIndex(2);
		
	
		oca = new OCA();
		for(int i = 0; i < 1; i++) {
			oca.addState(i);
		}
		oca.addTransition(0, OCAOp.Sub, 0);
		oca.setInitIndex(0);
		oca.setTargetIndex(0);
		
		oca = new OCA();
		for(int i = 0; i < 5; i++) {
			oca.addState(i);
		}
		oca.addTransition(0, OCAOp.Sub, 3);
		oca.addTransition(3, OCAOp.Add, 0);
		oca.addTransition(3, OCAOp.Add, 4);
		oca.addTransition(4, OCAOp.Sub, 1);
		oca.addTransition(1, OCAOp.Sub, 1);
		oca.addTransition(1, OCAOp.Add, 2);
		oca.addTransition(4, OCAOp.Add, 2);
		oca.addTransition(2, OCAOp.Sub, 3);
		oca.setInitIndex(0);
		oca.setTargetIndex(3);
		
			*/
		
		
		Converter con = new Converter(oca); 
		
		String result = con.convert();
		DataOutputStream out = new DataOutputStream(new FileOutputStream("/home/clexma/Desktop/test.smt"));
		out.writeChars("(declare-fun xs () Int) (declare-fun xt () Int)");
		out.writeChars("(assert");
		out.writeChars(result);
		out.writeChars(")");
		out.writeChars("(check-sat)");
		
		
		System.out.println("----------------------------OCA  INPUT----------------------------");
		oca.print();
		System.out.println("------------------------------------------------------------------");
		System.out.println("--------------------------FORMULA OUTPUT--------------------------");
		System.out.println(result);
		System.out.println("------------------------------------------------------------------");
		//}
	}
}
