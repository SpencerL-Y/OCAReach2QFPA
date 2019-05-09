package ocareach;

import java.util.ArrayList;
import java.util.List;

import com.microsoft.z3.*;

import automata.State;
import automata.counter.OCA;
import formula.generator.QFPAGenerator;
import graph.directed.DGEdge;
import graph.directed.DGFlowTuple;
import graph.directed.DGPath;
import graph.directed.DGVertex;
import graph.directed.DGraph;
import graph.directed.SDGVertex;
import graph.directed.SDGraph;
import graph.directed.abs.ASDGPath;
import graph.directed.abs.ASDGVertex;
import graph.directed.abs.ASDGraph;
import graph.directed.abs.BorderEdge;
import graph.directed.abs.LoopTag;
import table.dwt.DWTEntry;
import table.dwt.DWTuple;

public class Converter {
	// TODO: imple
	// input: A One-Counter Automaton and two states s, t of it
	// output: A QFPA formula that is satisfiable if there is a 
	// a possible run from (s,x) to (t,y) where x and y are integer
	// variable
	
	private OCA oca;
	private DGraph dgraph;
	private SDGraph sdg;
	private ASDGraph asdg;
	private QFPAGenerator qfpaGen;
	private State startState;
	private State endState;
	
	public Converter(OCA oca) {
		this.setOca(oca);
		this.dgraph = this.getOca().toDGraph();
		this.sdg = new SDGraph(this.getDgraph());
		this.qfpaGen = new QFPAGenerator();
	}
	
	
	// ALGORITHM
	public String convert(State startState, State endState) {
		assert(this.getOca().containState(startState) && this.getOca().containState(endState));
		//set starting and ending vertex in dg
		this.getDgraph().setStartVertexIndex(startState.getIndex());
		this.getDgraph().setEndingVertexIndex(endState.getIndex());
		// mark sccIndex in SDG
		this.getSdg().tarjan();
		// construct abstract sdg
		this.asdg = new ASDGraph(this.getSdg());
		ASDGVertex absStart = this.getAsdg().getVertex(this.getSdg().getStartingVertex().getSccMark());
		ASDGVertex absEnd = this.getAsdg().getVertex(this.getSdg().getEndingVertex().getSccMark());
		// get all the possible abstract path
		List<ASDGPath> paths = this.getAsdg().DFSFindAbsPaths(absStart.getSccIndex(), absEnd.getSccIndex());
		List<Expr> formulae = new ArrayList<Expr>();
		for(ASDGPath p : paths) {
			// there is no cycles in the SCCs (trivial case: every scc is a concrete vertex)
			boolean trivial = !p.containsCycledVertex();
			// there might be type-1 certificate
			boolean type1 = p.containsNegTagVertex();
			// there might be type-1 . type-2 certificate
			boolean type12 = p.containsPosTagVertex();
			// there might be type-1 . type-3 . type-2 certificate
			boolean type132 = p.containsNegTagVertex() && p.containsPosTagVertex();
			BoolExpr trivialForm = this.getQfpaGen().mkFalse();
			BoolExpr type1Form = this.getQfpaGen().mkFalse();
			BoolExpr type12Form = this.getQfpaGen().mkFalse();
			BoolExpr type132Form = this.getQfpaGen().mkFalse();
			IntExpr sVar = this.getQfpaGen().mkVariableInt("xs");
			IntExpr tVar = this.getQfpaGen().mkVariableInt("xt");
			if(trivial) {
				trivialForm = this.genTrivialFormula(p);
			}
			if(type1) {
				type1Form = this.genType1Formulae(p, startState.getIndex(), endState.getIndex(), sVar, tVar, false);
			}
			if(type12) {
				type12Form = this.genType12Formulae(p, startState.getIndex(), endState.getIndex(), sVar, tVar);
			}
			if(type132) {
				type132Form = this.genType132Formulae(p, startState.getIndex(), endState.getIndex(), sVar, tVar);
			}
			Expr temp = (trivial)? trivialForm : this.combineAllFormlae(type1Form, type12Form, type132Form);
			formulae.add(temp);
		}
		
		
		String result = null;
		
		//TODO imple
		return result;
	}
	
	private BoolExpr genTrivialFormula(ASDGPath p) {
		//TODO debug
		// convert abstract vertices to concrete vertices
		DGPath cp = new DGPath(p.getVertex(0).getConcreteDGraph(false).getVertices().get(0));
		for(int i = 1; i <= p.length(); i++) {
			cp.concatVertex(p.getVertex(i).getConcreteDGraph(false).getVertices().get(0));
		}
		IntExpr sVar = this.getQfpaGen().mkVariableInt("xs");
		IntExpr tVar = this.getQfpaGen().mkVariableInt("xt");
		BoolExpr result = this.getQfpaGen()
						  .mkAndBool(this.getQfpaGen().mkEqBool(this.getQfpaGen().mkSubInt(tVar, sVar), 
								  								this.getQfpaGen().mkConstantInt(cp.getWeight())),// weight correctly summed
								  	 this.getQfpaGen().mkGeBool(this.getQfpaGen().mkAddInt(sVar, this.getQfpaGen().mkConstantInt(cp.getDrop())), 
								  			 					this.getQfpaGen().mkConstantInt(0)), // counter value does not drop below 0
								  	 this.getQfpaGen().mkGeBool(sVar, this.getQfpaGen().mkConstantInt(0)), // sVar >= 0
								  	 this.getQfpaGen().mkGeBool(tVar, this.getQfpaGen().mkConstantInt(0)) // tVar >= 0
								    );
		return result;
	}
	
	// two kinds of implementation :
	// 1. guess support then determine which type certificate
	// 2. check the possible types of certificates then guess the support with requirements
	// Here we use the second one
	// TODO: debug trivial case from abstract state to concreate graph
	//TODO: debug
	private BoolExpr genType1Formulae(ASDGPath p, int startIndex, int endIndex,
									              IntExpr startVar, IntExpr endVar
									              , boolean isSkew) {
		//TODO imple
		//assertion
		IntExpr sVar = startVar;
		IntExpr tVar = endVar;
		List<List<SDGVertex>> allPossibleInOut = p.inportsOutportsCartesianProduct(p.getG().getSdg().getVertex(startIndex),
																				   p.getG().getSdg().getVertex(endIndex));
		//int inOutSeqSize = allPossibleInOut.get(0).size();
		IntExpr[] absPathVars = new IntExpr[2*(p.length())];
		for(int i = 0; i < 2*p.length(); i ++) {
			if(i % 2 == 0) {
				absPathVars[i] = this.getQfpaGen().mkVariableInt("v_o_" + p.getVertex((i)/2).getSccIndex());
			} else {
				absPathVars[i] = this.getQfpaGen().mkVariableInt("v_i_" + p.getVertex((i+1)/2).getSccIndex());
			}
		}
		BoolExpr type1FormBody = this.getQfpaGen().mkFalse();
		for(List<SDGVertex> l : allPossibleInOut) {
			// for every possible sequence of inoutports
			BoolExpr type1FormBodyItem = this.getQfpaGen().mkTrue();
			for(int i = 0; i <= p.length(); i ++) {
				BoolExpr currentAbsVertexForm = this.getQfpaGen().mkFalse();
				List<BoolExpr> sccExprs;
				if(i == 0) {
					sccExprs = this.genAbsStateNoPosCycle(p.getVertex(i), 
						p.getG().getSdg().getVertex(startIndex), l.get(2*i),
						null, p.getG().getBorderEdgeByInportOutport(l.get(2*i), l.get(2*i + 1)),
						sVar, absPathVars[2*i], 
						null, absPathVars[2*i + 1], isSkew);
				} else if(i == p.length()) {
					sccExprs = this.genAbsStateNoPosCycle(p.getVertex(i), 
						l.get(2*i-1), p.getG().getSdg().getVertex(endIndex),
						p.getG().getBorderEdgeByInportOutport(l.get(2*i - 2), l.get(2*i - 1)), null,
						absPathVars[2*i - 1], tVar, 
						absPathVars[2*i - 2], null, isSkew);
				} else {
					sccExprs = this.genAbsStateNoPosCycle(p.getVertex(i),
						l.get(2*i - 1), l.get(2*i), 
						p.getG().getBorderEdgeByInportOutport(l.get(2*i - 2), l.get(2*i - 1)), p.getG().getBorderEdgeByInportOutport(l.get(2*i), l.get(2*i + 1)),
						absPathVars[2*i - 1], absPathVars[2*i],
						absPathVars[2*i - 2], absPathVars[2*i + 1], isSkew);
				}
				for(BoolExpr e : sccExprs) {
					currentAbsVertexForm = this.getQfpaGen().mkOrBool(e, currentAbsVertexForm);
				}
				type1FormBodyItem = this.getQfpaGen().mkAndBool(type1FormBodyItem, currentAbsVertexForm);
			}
			type1FormBody = this.getQfpaGen().mkOrBool(type1FormBody, type1FormBodyItem);
		}
		// add ge 0 requirement
		BoolExpr posRequires = this.getQfpaGen().mkTrue();
		for(int i = 0; i < absPathVars.length; i++) {
			posRequires = this.getQfpaGen().mkAndBool(
				posRequires, 
				this.getQfpaGen().mkGeBool(absPathVars[i], this.getQfpaGen().mkConstantInt(0))
			);
		}
		type1FormBody = this.getQfpaGen().mkAndBool(type1FormBody, posRequires);
		Expr type1Form = this.getQfpaGen().mkExistsQuantifier(absPathVars, type1FormBody);
		return (BoolExpr) type1Form;
	}
	
	//TODO: debug
	//TODO: imple add variable positive requirement
	private List<BoolExpr> genAbsStateNoPosCycle(ASDGVertex v, SDGVertex inport, SDGVertex outport, 
														   BorderEdge in, BorderEdge out, 
														   IntExpr thisInVar,  IntExpr thisOutVar,
														   IntExpr lastOutVar, IntExpr nextInVar, boolean isSkew) {
		//TODO: imple add special case the start vertex and the end vertex
		assert(v.containIndex(inport.getVertexIndex()) && v.containIndex(outport.getVertexIndex()));
		assert(thisInVar != null && thisOutVar != null && v != null);
		if(in == null && out != null && lastOutVar == null && nextInVar != null) {
			// start absVertex
			return this.type1ConcreteGraphPathFormula(v, inport, outport, in, out, thisInVar, thisOutVar, lastOutVar, nextInVar, isSkew);
		} else if(out == null && nextInVar == null) {
			// end absVertex
			return this.type1ConcreteGraphPathFormula(v, inport, outport, in, out, thisInVar, thisOutVar, lastOutVar, nextInVar, isSkew);
		} else {
			// other absVertex
			return this.type1ConcreteGraphPathFormula(v, inport, outport, in, out, thisInVar, thisOutVar, lastOutVar, nextInVar, isSkew);
		}
	}
	
	//TODO: debug
	private BoolExpr borderEdgeWeightAndDropRequirements(BorderEdge in, BorderEdge out, 
													IntExpr thisInVar, IntExpr thisOutVar, 
													IntExpr nextInVar, IntExpr lastOutVar) {
		BoolExpr formula = this.getQfpaGen().mkAndBool(
				// border edge weight add correctly
				this.getQfpaGen().mkEqBool(
					this.getQfpaGen().mkSubInt(thisInVar, lastOutVar), 
					this.getQfpaGen().mkConstantInt(in.getWeight())),
				// border edge weight add correctly
				//TODO: the formula can be redundant here
				this.getQfpaGen().mkEqBool(
					this.getQfpaGen().mkSubInt(nextInVar, thisOutVar), 
					this.getQfpaGen().mkConstantInt(out.getWeight()))
		);
		return formula;
	}
	//TODO: debug
	private BoolExpr startVertexBorderEdgeWeigthAndDropRequirements(BorderEdge out, 
																	IntExpr thisInVar, IntExpr thisOutVar,
																	IntExpr nextInVar) {
		BoolExpr formula = this.getQfpaGen().mkEqBool(
					this.getQfpaGen().mkSubInt(nextInVar, thisOutVar), 
					this.getQfpaGen().mkConstantInt(out.getWeight()));
		return formula;
	}
	//TODO: debug
	private BoolExpr endVertexBorderEdgeWeightAndDropRequirements(BorderEdge in,
																  IntExpr thisInVar, IntExpr thisOutVar,
																  IntExpr lastOutVar) {
		BoolExpr formula = this.getQfpaGen().mkEqBool(
					this.getQfpaGen().mkSubInt(thisInVar, lastOutVar), 
					this.getQfpaGen().mkConstantInt(in.getWeight()));
		return formula;
	}
	//TODO: debug
	private List<BoolExpr> type1ConcreteGraphPathFormula(ASDGVertex v, SDGVertex inport, SDGVertex outport, 
			   										 BorderEdge in, BorderEdge out, 
			   										 IntExpr thisInVar,  IntExpr thisOutVar,
			   										 IntExpr lastOutVar, IntExpr nextInVar, boolean isSkew) {
		
		List<BoolExpr> exprs = new ArrayList<BoolExpr>();
		DGraph conGraph = v.getConcreteDGraph(isSkew);
		if(conGraph.getVertices().size() == 1) {
			// if the scc is trivial
			BoolExpr formula = this.getQfpaGen().mkAndBool(
				this.startVertexBorderEdgeWeigthAndDropRequirements(out, thisInVar, thisOutVar, nextInVar),
				this.getQfpaGen().mkEqBool(thisOutVar, thisInVar)
				
			);
			exprs.add(formula);
			return exprs;
		} 
		List<DGraph> supports = conGraph.getAllPossibleSupport(inport.getVertexIndex(), outport.getVertexIndex());
		for(DGraph support : supports) {
			// if there is positive cycle in the support ignore it
			if(support.computeLoopTag() != LoopTag.Pos && support.computeLoopTag() != LoopTag.PosNeg) {
				continue;
			}
			if(support.containsCycle()) {
				//TODO: debug
				// increase the max length to 3n^2 + 1
				support.increaseDWTLenLimit();
				// guess that there is a cycle and apply the lemma
				// length > 3n^2
				BoolExpr formGe = this.getQfpaGen().mkFalse();
				// guess the mid vertex and init a int variable for it
				for(DGVertex ve : support.getVertices()) {
					IntExpr midVar = this.getQfpaGen().mkVariableInt("z"+ ve.getIndex());
					formGe = this.getQfpaGen().mkOrBool(
						formGe, 
						this.getQfpaGen().mkAndBool(
							this.genPathFlowFormula(support, inport.getVertexIndex(), ve.getIndex(), thisInVar, midVar),
							// z > |V|, this gurantee the counter value to be positive
							this.getQfpaGen().mkGeBool(midVar, this.getQfpaGen().mkConstantInt(support.getVertices().size()))
						)
					);
				}
				// length < 3n^2 + 1
				BoolExpr formLt = this.getQfpaGen().mkFalse();
				for(DWTuple t : support.getTable().getEntry(inport.getVertexIndex(), outport.getVertexIndex()).getSetOfDWTuples()) {
					BoolExpr temp = this.getQfpaGen().mkAndBool(
						// weight sum correctly in the concreteScc
						this.getQfpaGen().mkEqBool(
							thisOutVar, 
							this.getQfpaGen().mkAddInt(thisInVar, this.getQfpaGen().mkConstantInt(t.getWeight()))),
							// the minimum counter value >= 0
						this.getQfpaGen().mkGeBool(
								this.getQfpaGen().mkAddInt(thisInVar, this.getQfpaGen().mkConstantInt(t.getDrop())), 
								this.getQfpaGen().mkConstantInt(0))
					);
					formLt = this.getQfpaGen().mkOrBool(formLt, temp);
				} 
				formLt = this.getQfpaGen().mkAndBool(formLt, 
						this.borderEdgeWeightAndDropRequirements(in, out, thisInVar, thisOutVar, nextInVar, lastOutVar));
				BoolExpr formula = this.getQfpaGen().mkOrBool(formLt, formGe);
				exprs.add(formula);
			} else {
				BoolExpr concretePathFormula = this.getQfpaGen().mkFalse();
				DWTEntry entry = support.getTable().getEntry(inport.getVertexIndex(), outport.getVertexIndex());
				for(DWTuple t : entry.getSetOfDWTuples()) {
					concretePathFormula = this.getQfpaGen().mkOrBool(
						concretePathFormula, 
						this.getQfpaGen().mkAndBool(
							// weight sum correctly in the concreteScc
							this.getQfpaGen().mkEqBool(
								thisOutVar, 
								this.getQfpaGen().mkAddInt(thisInVar, this.getQfpaGen().mkConstantInt(t.getWeight()))),
								// the minimum counter value >= 0
							this.getQfpaGen().mkGeBool(
									this.getQfpaGen().mkAddInt(thisInVar, this.getQfpaGen().mkConstantInt(t.getDrop())), 
									this.getQfpaGen().mkConstantInt(0))
						)
					);
				}
				// guess it is a simple path
				BoolExpr formula = this.getQfpaGen().mkAndBool(
					this.borderEdgeWeightAndDropRequirements(in, out, thisInVar, thisOutVar, nextInVar, lastOutVar),
					// there is a concrete path from inport to outport in concreteScc
					concretePathFormula
				);
				exprs.add(formula);
			}
		}
		return exprs;
	}
	
	private BoolExpr genPathFlowFormula(DGraph g, int startIndex, int endIndex,
													   IntExpr startVar, IntExpr endVar) {
		//TODO debug
		List<DGEdge> edgeList = g.getEdges();
		IntExpr[] flowVars = new IntExpr[edgeList.size()];
		DGFlowTuple[] flowTuples = new DGFlowTuple[edgeList.size()];
		for(int i = 0; i < edgeList.size(); i ++) {
			flowVars[i] = this.getQfpaGen().mkVariableInt("f_" + edgeList.get(i).getFrom().getIndex() + "_" +
																 edgeList.get(i).getTo().getIndex());
			flowTuples[i] = new DGFlowTuple(edgeList.get(i),flowVars[i]);
		}
		if(g.isConnected()) {
			DGVertex vs = g.getVertex(startIndex);
			DGVertex vt = g.getVertex(endIndex);
			BoolExpr body = this.getQfpaGen().mkTrue();
			// start vertex flow requirements
			BoolExpr startVertexForm = this.getQfpaGen().mkEqBool(
				this.getQfpaGen().mkAddInt(this.getQfpaGen().sumUpVars(this.getAllFlowInVars(vs, flowTuples)), this.getQfpaGen().mkConstantInt(1)),
				this.getQfpaGen().sumUpVars(this.getAllFlowOutVars(vs, flowTuples))
			);
			// end vertex flow requirements
			BoolExpr endVertexForm = this.getQfpaGen().mkEqBool(
				this.getQfpaGen().sumUpVars(this.getAllFlowInVars(vt, flowTuples)), 
				this.getQfpaGen().mkAddInt(this.getQfpaGen().sumUpVars(this.getAllFlowOutVars(vt, flowTuples)), this.getQfpaGen().mkConstantInt(1))
			);
			body = this.getQfpaGen().mkAndBool(startVertexForm, endVertexForm);
			for(DGVertex v : g.getVertices()) {
				// all other internal vertices flow requirements
				if(v.getIndex() != startIndex && v.getIndex() != endIndex) {
					BoolExpr vertexForm = this.getQfpaGen().mkEqBool(
						this.getQfpaGen().sumUpVars(this.getAllFlowInVars(v, flowTuples)), 
						this.getQfpaGen().sumUpVars(this.getAllFlowOutVars(v, flowTuples)));
					body = this.getQfpaGen().mkAndBool(vertexForm, body);
				}
			}
			// weight requirements
			// drop requirements are guranteed by the 3n^2 + 1 or type3 certificate
			IntExpr sum = this.getQfpaGen().mkConstantInt(0);
			for(DGFlowTuple t : flowTuples) {
				sum = this.getQfpaGen().mkAddInt(
					sum, 
					this.getQfpaGen().mkScalarTimes(this.getQfpaGen().mkConstantInt(t.getEdge().getWeight()), t.getEdgeVar())
				);
			}
			BoolExpr weightSumCorrect = this.getQfpaGen().mkEqBool(sum, this.getQfpaGen().mkSubInt(endVar, startVar));
			body = this.getQfpaGen().mkAndBool(body, weightSumCorrect);
			//TODO: debug BoolExpr type
			BoolExpr result = (BoolExpr) this.getQfpaGen().mkExistsQuantifier(flowVars, body);
			return result;
		} else {
			return this.getQfpaGen().mkFalse();
		}
	}
	//TODO: debug
	private List<IntExpr> getAllFlowInVars(DGVertex v, DGFlowTuple[] flowTuples){
		List<IntExpr> inVars = new ArrayList<IntExpr>();
		for(DGFlowTuple t : flowTuples) {
			if(t.getEdgeTo().getIndex() == v.getIndex()) {
				inVars.add(t.getEdgeVar());
			}
		}
		return inVars;
	}
	//TODO: debug
	private List<IntExpr> getAllFlowOutVars(DGVertex v, DGFlowTuple[] flowTuples){
		List<IntExpr> outVars = new ArrayList<IntExpr>();
		for(DGFlowTuple t : flowTuples) {
			if(t.getEdgeTo().getIndex() == v.getIndex()) {
				outVars.add(t.getEdgeVar());
			}
		}
		return outVars;
	}	
	
	//TODO: debug
	private BoolExpr genType12Formulae(ASDGPath p, int startIndex, int endIndex,
												   IntExpr startVar, IntExpr endVar) {
		// assert that there exists a postive tag absVertex
		assert(p.containsPosTagVertex());
		List<ASDGVertex> posVertices = p.getAllType12Split();
		
		// split the absPath
		IntExpr[] splitVars = new IntExpr[2];
		splitVars[0] = this.getQfpaGen().mkVariableInt("vt_1");
		splitVars[1] = this.getQfpaGen().mkVariableInt("vs_2");
		BoolExpr type12Form = this.getQfpaGen().mkFalse();
		for(ASDGVertex v : posVertices) {
			ASDGPath[] paths = p.splitPathAt(v);
			//for all the possible splited borderedges
			BoolExpr borderEdgesFormula = this.getQfpaGen().mkFalse();
			for(BorderEdge e : p.getG().getBorderEdgesByAbsEdge(paths[0].getLastVertex().getSccIndex(), 
																paths[1].getInit().getSccIndex())) {
				BoolExpr expr1 = this.genType1Formulae(paths[0], startIndex, e.getFromVertex().getVertexIndex(), startVar, splitVars[0], false);
				BoolExpr expr2 = this.genType1Formulae(paths[1].getSkewPath(), endIndex, e.getToVertex().getVertexIndex(), endVar, splitVars[1], true);
				borderEdgesFormula = this.getQfpaGen().mkOrBool(
					this.getQfpaGen().mkAndBool(
						expr1,
						expr2,
						this.getQfpaGen().mkEqBool(
							this.getQfpaGen().mkAddInt(
								splitVars[0], 
								this.getQfpaGen().mkConstantInt(e.getWeight()
							)),
							splitVars[1])),
					borderEdgesFormula
				);
			}
			type12Form = this.getQfpaGen().mkOrBool(
				type12Form,
				borderEdgesFormula
			);
		}
		type12Form = (BoolExpr) this.getQfpaGen().mkExistsQuantifier(splitVars, type12Form);
		return type12Form;
	}
	
	
	
	private BoolExpr genType132Formulae(ASDGPath p, int startIndex, int endIndex, 
												 IntExpr startVar, IntExpr endVar) {
		//TODO: debug
		// assert there is a condition for type132
		assert(p.containsNegTagVertex() && p.containsNegTagVertex());
		List<ASDGVertex[]> splitVertices = p.getAllType132Split();
		IntExpr[] splitVars = new IntExpr[4];
		splitVars[0] = this.getQfpaGen().mkVariableInt("vt_1");
		splitVars[1] = this.getQfpaGen().mkVariableInt("vs_3");
		splitVars[2] = this.getQfpaGen().mkVariableInt("vt_3");
		splitVars[3] = this.getQfpaGen().mkVariableInt("vs_2");
		BoolExpr type132Form = this.getQfpaGen().mkFalse();
		for(ASDGVertex[] s : splitVertices) {
			ASDGPath[] paths = p.getAllType132SplitPaths(s);
			BoolExpr portForm = this.getQfpaGen().mkTrue();
			for(SDGVertex[] inouts : p.getType132LinkInportOutport(s)) {
				portForm = this.getQfpaGen().mkAndBool(
						this.genType1Formulae(paths[0], startIndex, inouts[0].getVertexIndex(), startVar, splitVars[0], false),
						this.genType3Formula(paths[1], inouts[1].getVertexIndex(), inouts[2].getVertexIndex(), splitVars[1], splitVars[2]),
						this.genType1Formulae(paths[2], inouts[3].getVertexIndex(), endIndex, splitVars[3], endVar, true),
						this.getQfpaGen().mkEqBool(
								splitVars[1], 
								this.getQfpaGen().mkAddInt(
										splitVars[0], 
										this.getQfpaGen().mkConstantInt(p.getG().getBorderEdgeByInportOutport(inouts[0], inouts[1]).getWeight())
								)
						),
						this.getQfpaGen().mkEqBool(
								splitVars[3], 
								this.getQfpaGen().mkAddInt(
										splitVars[3], 
										this.getQfpaGen().mkConstantInt(p.getG().getBorderEdgeByInportOutport(inouts[2], inouts[3]).getWeight())
								)
						)
						
				);
			}
			type132Form = this.getQfpaGen().mkOrBool(portForm, type132Form);
		}
		type132Form = this.getQfpaGen().mkAndBool(
				type132Form,
				this.genVarNonNegRequirement(splitVars)
		);
		type132Form = (BoolExpr) this.getQfpaGen().mkExistsQuantifier(splitVars, type132Form);
		return type132Form;
		
	}
	
	private BoolExpr genVarNonNegRequirement(IntExpr[] vars) {
		BoolExpr result = this.getQfpaGen().mkTrue();
		for(IntExpr var : vars) {
			this.getQfpaGen().mkAndBool(
					result,
					this.getQfpaGen().mkGeBool(var, this.getQfpaGen().mkConstantInt(0))
			);
		}
		return result;
	}
	
	//TODO: debug
	private BoolExpr genType3Formula(ASDGPath p3, int startIndex, int endIndex,
												  IntExpr startVar, IntExpr endVar) {
		ASDGVertex startVertex = p3.getInit();
		ASDGVertex endVertex   = p3.getLastVertex();
		assert(startVertex.containIndex(startIndex) && endVertex.containIndex(endIndex));
		DGraph p3Graph = this.genType3ConcreteGraph(p3);
		DGraph p3SkewGraph = p3Graph.getSkewTranspose();
		assert(p3Graph.containsVertex(startIndex) && p3Graph.containsVertex(endIndex));
		IntExpr startMidVar = this.getQfpaGen().mkVariableInt("vm_3_s");
		IntExpr endMidVar = this.getQfpaGen().mkVariableInt("vm_3_e");
		IntExpr[] boundVars = new IntExpr[2];
		boundVars[0] = startMidVar;
		boundVars[1] = endMidVar;
		BoolExpr startPosTemp = this.getQfpaGen().mkFalse();
		BoolExpr endPosTemp = this.getQfpaGen().mkFalse();
		for(DGVertex vms : p3Graph.getVertices()) {
			startPosTemp = this.getQfpaGen().mkOrBool(startPosTemp, this.genPosCycleTemplateFormula(p3Graph, startIndex, vms.getIndex(), startVar, startMidVar));
		}
		for(DGVertex vme : p3SkewGraph.getVertices()) {
			endPosTemp = this.getQfpaGen().mkOrBool(endPosTemp, this.genPosCycleTemplateFormula(p3SkewGraph, endIndex, vme.getIndex(), endVar, endMidVar));
		}
		BoolExpr posTemp = (BoolExpr) this.getQfpaGen().mkExistsQuantifier(boundVars, this.getQfpaGen().mkAndBool(
																											startPosTemp, endPosTemp,
																											this.genVarNonNegRequirement(boundVars)));
		BoolExpr pathFlow = this.genPathFlowFormula(p3Graph, startIndex, endIndex, startVar, endVar);
		BoolExpr result = this.getQfpaGen().mkAndBool(
				posTemp, 
				pathFlow
		);
		return result;
	}
	
	private DGraph genType3ConcreteGraph(ASDGPath p3) {
		// generate the graph
		List<DGEdge> concreteEdgeList = new ArrayList<DGEdge>();
		for(int i = 0; i < p3.getPath().size() - 1; i++) {
			List<BorderEdge> borderEdges = p3.getVertex(i).getAllConcreteEdgesTo(p3.getVertex(i+1));
			for(BorderEdge e : borderEdges) {
				concreteEdgeList.add(e.getConcreteEdge());
			}
		}
		for(ASDGVertex v : p3.getPath()) {
			for(DGEdge e : v.getConcreteDGraph(false).getEdges()) {
				concreteEdgeList.add(e);
			}
		}
		DGraph type3Graph = p3.getVertex(0).getGraph().getSdg().getGraph().edgeListToGraph(concreteEdgeList);
		return type3Graph;
	}
	
	//TODO: formula for postive cycle template
	
	private BoolExpr genPosCycleTemplateFormula(DGraph g, int startIndex, int guessIndex, IntExpr startVar, IntExpr guessVar) {
		//TODO: imple
		// desciption: return the possible positive cycle template with the minimum drop, if none return null
		assert(g.containsVertex(startIndex) && g.containsVertex(guessIndex));
		if(g.getTag() == null) {
			g.computeLoopTag();
		}
		BoolExpr form = this.getQfpaGen().mkFalse();
		for(DWTuple t : g.getTable().getEntry(startIndex, guessIndex).getSetOfDWTuples()) {
			for(DWTuple tg : g.getTable().getEntry(guessIndex, guessIndex).getSetOfDWTuples()) {
				if(tg.getWeight() > 0) {
					int drop = Math.min(t.getDrop(), tg.getDrop() + t.getWeight());
					form = this.getQfpaGen().mkOrBool(
						form,
						this.getQfpaGen().mkAndBool(
							this.getQfpaGen().mkGeBool(this.getQfpaGen().mkAddInt(startVar, this.getQfpaGen().mkConstantInt(drop)), this.getQfpaGen().mkConstantInt(0)),
							this.getQfpaGen().mkEqBool(this.getQfpaGen().mkAddInt(startVar, this.getQfpaGen().mkConstantInt(t.getWeight())), guessVar)
						)
					);
				} 
			}
		}
		return form;
	}
	
	private Expr combineAllFormlae(BoolExpr type1, BoolExpr type12, BoolExpr type132) {
		Expr result = this.getQfpaGen().mkOrBool(
			type1, type12, type132
		);
		return result;
	}
	
	//getters and setters
	public OCA getOca() {
		return oca;
	}
	public void setOca(OCA oca) {
		this.oca = oca;
	}
	public SDGraph getSdg() {
		return sdg;
	}
	public void setSdg(SDGraph sdg) {
		this.sdg = sdg;
	}
	public DGraph getDgraph() {
		return dgraph;
	}
	public void setDgraph(DGraph dgraph) {
		this.dgraph = dgraph;
	}
	public QFPAGenerator getQfpaGen() {
		return qfpaGen;
	}
	public void setQfpaGen(QFPAGenerator qfpaGen) {
		this.qfpaGen = qfpaGen;
	}
	public ASDGraph getAsdg() {
		return asdg;
	}
	public void setAsdg(ASDGraph asdg) {
		this.asdg = asdg;
	}

	public State getStartState() {
		return startState;
	}

	public void setStartState(State startState) {
		this.startState = startState;
	}

	public State getEndState() {
		return endState;
	}

	public void setEndState(State endState) {
		this.endState = endState;
	}
	
}