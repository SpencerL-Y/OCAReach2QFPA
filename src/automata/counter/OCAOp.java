package automata.counter;

import automata.Operation;

public enum OCAOp implements Operation{
	Add,
	Sub,
	Zero;

	@Override
	public String getLabel() {
		return null;
	}
}
