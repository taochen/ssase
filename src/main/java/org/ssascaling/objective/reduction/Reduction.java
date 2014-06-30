package org.ssascaling.objective.reduction;

import java.util.List;

import org.ssascaling.objective.Objective;

/**
 * The reduction and the updating of models within the region should be synchronized.
 * @author tao
 *
 */
public interface Reduction {

	public List<List<Objective>>  doReduction (List<Objective> objectives);
}
