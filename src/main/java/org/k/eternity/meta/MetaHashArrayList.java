package org.k.eternity.meta;

import java.util.ArrayList;
import java.util.List;

public class MetaHashArrayList extends ArrayList<G2> {

	public MetaHashArrayList(List<G2> countG2For) {
		if(countG2For != null){
			this.addAll(countG2For);
		}
	}

	public MetaHashArrayList() {
	}
}
