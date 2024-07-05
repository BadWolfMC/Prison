/*
 *  Prison is a Minecraft plugin for the prison game mode.
 *  Copyright (C) 2017-2020 The Prison Team
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package tech.mcprison.prison;

import java.util.ArrayList;
import java.util.List;

import tech.mcprison.prison.internal.ArmorStand;
import tech.mcprison.prison.internal.Entity;
import tech.mcprison.prison.internal.EntityType;
import tech.mcprison.prison.internal.Player;
import tech.mcprison.prison.internal.PrisonStatsElapsedTimeNanos;
import tech.mcprison.prison.internal.World;
import tech.mcprison.prison.internal.block.Block;
import tech.mcprison.prison.internal.block.MineResetType;
import tech.mcprison.prison.internal.block.MineTargetPrisonBlock;
import tech.mcprison.prison.internal.block.PrisonBlock;
import tech.mcprison.prison.util.Location;

/**
 * @author Faizaan A. Datoo
 */
public class TestWorld implements World {

    String name;

    public TestWorld(String name) {
        this.name = name;
    }

    @Override 
    public String getName() {
        return name;
    }

    @Override 
    public List<Player> getPlayers() {
        return null;
    }

    @Override 
    public Block getBlockAt(Location location) {
        return null;
    }
    
    @Override
    public Block getBlockAt( Location location, boolean containsCustomBlocks )
    {
    	return null;
    }

    @Override
    public void setBlock( PrisonBlock block, int x, int y, int z ) {
    }

	@Override
	public void setBlockAsync( PrisonBlock prisonBlock, Location location ) {
	}
	
	@Override
	public void setBlocksSynchronously( List<MineTargetPrisonBlock> tBlocks, 
							MineResetType resetType,
							PrisonStatsElapsedTimeNanos nanos ) {
		
	}

	@Override
	public List<Entity> getEntities() {
		return new ArrayList<>();
	}

	@Override
	public Entity spawnEntity( Location location, EntityType entityType ) {
		return null;
	}

	@Override
	public ArmorStand spawnArmorStand(Location location) {
		return null;
	}


}
