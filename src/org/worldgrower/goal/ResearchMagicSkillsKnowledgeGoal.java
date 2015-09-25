/*******************************************************************************
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.worldgrower.goal;

import java.util.List;

import org.worldgrower.Constants;
import org.worldgrower.OperationInfo;
import org.worldgrower.World;
import org.worldgrower.WorldObject;
import org.worldgrower.actions.Actions;
import org.worldgrower.actions.ResearchKnowledgeSkillAction;
import org.worldgrower.attribute.SkillProperty;

public class ResearchMagicSkillsKnowledgeGoal implements Goal {
	
	private static final SkillProperty[] MAGIC_SKILLS = { Constants.EVOCATION_SKILL, Constants.RESTORATION_SKILL, Constants.NECROMANCY_SKILL, Constants.ILLUSION_SKILL };

	@Override
	public OperationInfo calculateGoal(WorldObject performer, World world) {
		List<WorldObject> libraries = LibraryUtils.getLibraries(world);
		if (libraries.size() > 0) {
			WorldObject library = libraries.get(0);
			
			for(SkillProperty skillProperty : MAGIC_SKILLS) {
				if (performer.getProperty(skillProperty).getLevel() < 10) {
					ResearchKnowledgeSkillAction researchKnowledgeSkillAction = Actions.getResearchKnowledgeSkillActionFor(skillProperty);
					return new OperationInfo(performer, library, new int[0], researchKnowledgeSkillAction);
				}
			}
		} else {
			return new LibraryGoal().calculateGoal(performer, world);
		}
		return null;
	}
	
	@Override
	public void goalMetOrNot(WorldObject performer, World world, boolean goalMet) {
	}

	@Override
	public boolean isGoalMet(WorldObject performer, World world) {
		boolean allSkillsAre10OrHigher = false;
		for(SkillProperty skillProperty : MAGIC_SKILLS) {
			if (performer.getProperty(skillProperty).getLevel() >= 10) {
				allSkillsAre10OrHigher = true;
			}
		}
		return allSkillsAre10OrHigher;
	}
	
	@Override
	public boolean isUrgentGoalMet(WorldObject performer, World world) {
		return isGoalMet(performer, world);
	}

	@Override
	public String getDescription() {
		return "researching magic skills";
	}

	@Override
	public int evaluate(WorldObject performer, World world) {
		int sumOfSkillLevels = 0;
		for(SkillProperty skillProperty : MAGIC_SKILLS) {
			sumOfSkillLevels += performer.getProperty(skillProperty).getLevel();
		}
		return sumOfSkillLevels;
	}
}
