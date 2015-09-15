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

import org.worldgrower.OperationInfo;
import org.worldgrower.World;
import org.worldgrower.WorldObject;
import org.worldgrower.actions.Actions;
import org.worldgrower.actions.BuildArenaAction;

public class ArenaGoal implements Goal {

	@Override
	public OperationInfo calculateGoal(WorldObject performer, World world) {
		if (!BuildArenaAction.hasEnoughStone(performer)) {
			return new StoneGoal().calculateGoal(performer, world);
		} else {
			WorldObject target = BuildLocationUtils.findOpenLocationNearExistingProperty(performer, 10, 11, world);
			return new OperationInfo(performer, target, new int[0], Actions.BUILD_ARENA_ACTION);
		}
	}
	
	@Override
	public void goalMetOrNot(WorldObject performer, World world, boolean goalMet) {
	}

	@Override
	public boolean isGoalMet(WorldObject performer, World world) {
		return ArenaPropertyUtils.worldObjectOwnsArena(performer);
	}
	
	@Override
	public boolean isUrgentGoalMet(WorldObject performer, World world) {
		return isGoalMet(performer, world);
	}

	@Override
	public String getDescription() {
		return "building an arena";
	}

	@Override
	public int evaluate(WorldObject performer, World world) {
		return ArenaPropertyUtils.worldObjectOwnsArena(performer) ? 1 : 0;
	}

}