/*
 * Copyright (C) 2017 The MC-Prison Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package tech.mcprison.prison.ranks.managers;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import tech.mcprison.prison.Prison;
import tech.mcprison.prison.output.Output;
import tech.mcprison.prison.ranks.PrisonRanks;
import tech.mcprison.prison.ranks.data.Rank;
import tech.mcprison.prison.ranks.data.RankLadder;
import tech.mcprison.prison.ranks.data.RankLadderFactory;
import tech.mcprison.prison.ranks.data.RankPlayer;
import tech.mcprison.prison.ranks.data.RankPlayerFactory;
import tech.mcprison.prison.store.Collection;
import tech.mcprison.prison.store.Document;

/**
 * Manages the creation, removal, and management of ladders.
 *
 * @author Faizaan A. Datoo
 */
public class LadderManager
		extends LadderManagerMessages {
	
	public static final String LADDER_DEFAULT = "default";
	public static final String LADDER_PRESTIGES = "prestiges";

    /*
     * Fields & Constants
     */

    private Collection collection;
    private List<RankLadder> loadedLadders;
    
    private PrisonRanks prisonRanks;

    /*
     * Constructor
     */

    /**
     * Instantiate this {@link LadderManager}.
     * @param prisonRanks 
     */
    public LadderManager(Collection collection, PrisonRanks prisonRanks) {
        this.collection = collection;
        this.loadedLadders = new ArrayList<>();
        
        this.prisonRanks = prisonRanks;
    }

    /*
     * Methods & Getters & Setters
     */

    /**
     * Loads a ladder from a file into the loaded ladders list.
     * After this method is called, the ladder will be ready for use in the server.
     *
     * @param fileKey The key that this ladder is stored as. This is case-sensitive.
     * @throws IOException If the file could not be read or does not exist.
     */
    public void loadLadder(String fileKey, RankManager rankManager) throws IOException {
        
    	Document doc = collection.get(fileKey).orElseThrow(IOException::new);
        
        RankLadderFactory rlFactory = new RankLadderFactory();
        
        RankLadder ladder = rlFactory.createRankLadder(doc, rankManager);
        loadedLadders.add(ladder);
        
        // Will be dirty if load a ladder and the rank name does not exist and it adds them:
        if ( ladder.isDirty() ) {
        	saveLadder(ladder);
        }
    }

    /**
     * Loads every {@link RankLadder} stored to disk.
     *
     * @throws IOException If the folder could not be found, or if a file could not be read or does not exist.
     */
    public void loadLadders( RankManager rankManager ) 
    		throws IOException {
        List<Document> documents = collection.getAll();
        
        final RankLadderFactory rlFactory = new RankLadderFactory();
        
        documents.forEach(document -> loadedLadders.add(
        						rlFactory.createRankLadder(document, rankManager)) );
        
        for ( RankLadder ladder : loadedLadders ) {
        	
        	// If old file exists, then set dirty so it can be saved and update the file name:
        	checkIfOldFileExists( ladder );

        	// Will be dirty if load a ladder and the rank name does not exist and it adds them:
        	if ( ladder.isDirty() ) {
        		saveLadder(ladder);
        	}
		}
    }

    /**
     * If the old file name exists, then this ladder has not been upgraded
     * yet.  So set it to dirty so it can be saved and update the file name.
     * 
     * @param ladder
     */
    private void checkIfOldFileExists(RankLadder ladder) {
    	if ( collection.exists( getLadderNameOld(ladder) )) {
    		ladder.setDirty( true );
    	}
	}

	/**
     * Saves a ladder to its save file.
     *
     * @param ladder  The {@link RankLadder} to save.
     * @param fileKey The key to write the ladder as.
     * @param oldFileName The old file name based upon id.
     * @throws IOException If the ladder could not be serialized, or if the ladder could not be saved to the file.
     */
    public void saveLadder(RankLadder ladder, String fileKey, String oldFileName) throws IOException {
        collection.save(fileKey, ladder.toDocument(), oldFileName, "Ladder");
    }

    /**
     * Saves a ladder to its save file.
     *
     * @param ladder The {@link RankLadder} to save.
     * @throws IOException If the ladder could not be serialized, or if the ladder could not be saved to the file.
     */
    private void saveLadder(RankLadder ladder) throws IOException {
    	
    	String fileLadderNameNew = getLadderNameNew(ladder);
    	String fileLadderNameOld = getLadderNameOld(ladder);
    	
        this.saveLadder(ladder, fileLadderNameNew, fileLadderNameOld);
    }

    private String getLadderNameNew( RankLadder ladder ) {
    	return "ladder_" + ladder.getName();
    }
    
    /**
     * This function will generate an old ladder name only if the id is not
     * -1.  If it's -1, then there is no need to return anything other than null.
     * This will be used to identify if an old file exists so it can be removed
     * when the newer format is saved.
     * 
     * @param ladder
     * @return
     */
    private String getLadderNameOld( RankLadder ladder ) {
    	return ladder.getId() == -1 ? null : "ladder_" + ladder.getId();
    }
    
    /**
     * <p>This is the save function that should be used from outside of the LadderManager, such as
     * within the LadderCommands functions because this will be able to handle the possible 
     * exceptions that are thrown if there are any IOExceptions.  If there is a failure, then
     * it will log the failure to the console, but it will not notify the user; that's what
     * the return value is supposed to provide: success or failure.
     * </p>
     * 
     * <p>This will try to save the ladder, and if successful, then it will return a value of 
     * true, otherwise a value of false will indicate that there was a failure.
     * </p>
     * 
     * @param ladder
     * @return success or failure.  A value of true indicates the save was successful.
     */
    public boolean save( RankLadder ladder ) {
    	boolean success = false;
    	
    	try {
    		saveLadder( ladder );
    		success = true;
    	}
    	catch ( IOException e ) {
    		
    		String errorMessage = cannotSaveLadderFile( ladder.getName(), e.getMessage() );
    		
    		Output.get().logError( errorMessage, e );
    	}
    	
    	return success;
    }
    
    /**
     * Saves all the loaded ladders to their own files within a directory.
     * Each ladder file will be assigned a name in the format: ladder_&lt;ladder id&gt;.
     *
     * @throws IOException If the ladderFolder does not exist, or if one of the ladders could not be saved.
     */
    public void saveLadders() throws IOException {
        for (RankLadder ladder : loadedLadders) {
            saveLadder(ladder);
        }
    }

    /**
     * Creates a new ladder with the specified parameters.
     * This new ladder will be loaded, but will not be written to disk until {@link #saveLadder(RankLadder, String)} is called.
     *
     * @param name The name of this ladder, for use with the user (i.e. this will be shown to the user).
     * @return A {@link RankLadder} if it could be created, or null if the ladder's creation failed.
     */
    public RankLadder createLadder(String name) {
        // Set the default values...
    	// ladder id is no longer used, so use -1:
        RankLadder newLadder = new RankLadder( -1, name );
//        RankLadder newLadder = new RankLadder( getNextAvailableId(), name );

        // ... add it to the list...
        loadedLadders.add(newLadder);

        // ...and return it.
        return newLadder;
    }

    /**
     * Returns the next available ID for a new ladder.
     * This works by adding one to the highest current ladder ID.
     *
     * @return The next available ladder's ID.
     */
    @SuppressWarnings("unused")
	private int getNextAvailableId() {
        // Set the highest to -1 for now, since we'll add one at the end
        int highest = -1;

        // If anything's higher, it's now the highest...
        for (RankLadder ladder : loadedLadders) {
            if (highest < ladder.getId()) {
                highest = ladder.getId();
            }
        }

        return highest + 1;
    }

    /**
     * Removes the provided ladder. This will go through the process of removing the ladder from the loaded
     * ladders list, removing the ladder's save files, removing the ranks from the ladder, and handling the affected players.
     * This is a destructive operation; be sure that you are using it in the correct manner.
     *
     * @param ladder The {@link RankLadder} to be removed.
     * @return true if the ladder was removed successfully, false otherwise.
     */
    public boolean removeLadder(RankLadder ladder) {

        // Remove the players from the ladder
        List<RankPlayer> playersWithLadder =
            PrisonRanks.getInstance().getPlayerManager().getPlayers()
            	.stream()
                .filter(rankPlayer -> rankPlayer.hasLadder(ladder.getName()))
                .collect(Collectors.toList());
        
        RankPlayerFactory rankPlayerFactory = new RankPlayerFactory();
        
        for (RankPlayer player : playersWithLadder) {
            rankPlayerFactory.removeLadder( player, ladder.getName() );
        }

        // Remove it from the list...
        loadedLadders.remove(ladder);

        // ... and remove the ladder's save files.
        collection.delete( getLadderNameNew(ladder) );
        collection.delete( getLadderNameOld(ladder) );
        
//        collection.remove("ladder_" + ladder.id);
        return true;
    }

    public RankLadder getLadderDefault() {
    	return getLadder( RankLadder.DEFAULT );
    }
    
    public RankLadder getLadderPrestiges() {
    	return getLadder( RankLadder.PRESTIGES );
    }
    
    /**
     * Returns the ladder with the specified name.
     *
     * @param name The ladder's name, case-sensitive.
     * @return An optional containing either the {@link RankLadder} if it could be found, or empty if it does not exist by the specified name.
     */
    public RankLadder getLadder(String name) {
    	RankLadder results = null;
    	for ( RankLadder rankLadder : loadedLadders ) {
			if ( rankLadder.getName().equalsIgnoreCase( name ) ) {
				results = rankLadder;
				break;
			}
		}
    	return results;
    }

    /**
     * Returns the ladder with the specified ID.
     *
     * @param id The ladder's ID.
     * @return the {@link RankLadder} if it could be found, or null if it does not exist by the specified id.
     */
    public RankLadder getLadder(int id) {
    	RankLadder results = null;
    	for ( RankLadder rankLadder : loadedLadders ) {
			if ( rankLadder.getId() == id ) {
				results = rankLadder;
				break;
			}
		}
    	return results;
    }

    /**
     * Returns a list of all the loaded ladders on the server.
     *
     * @return A {@link List}. This will never return null, because if there are no loaded ladders, the list will just be empty.
     */
    public List<RankLadder> getLadders() {
        return loadedLadders;
    }

    /**
     * Returns a list of the ladders which contain a rank.
     * If the server is set up correctly, the list should never be empty (the default ladder will at least be present). However,
     * it is safer to check for this condition for a fail-safe.
     *
     * @param rankId The ID of the rank to check each ladder against.
     * @return A list of {@link RankLadder}s with the matched criteria.
     */
//    public List<RankLadder> getLaddersWithRank(int rankId) {
//        return loadedLadders.stream().filter(rankLadder -> rankLadder.containsRank(rankId))
//            .collect(Collectors.toList());
//    }

    /**
     * <p>This returns the ladder that has the specified rank.  The ladder 
     * should already be tied to the rank
     * @param rank
     * @return
     */
	public RankLadder getLadder( Rank rank ) {
		RankLadder results = null;
		
		for ( RankLadder rankLadder : loadedLadders ) {
			if ( rankLadder.containsRank( rank )) {
				results = rankLadder;
				break;
			}
		}

		return results;
	}

	
	public String printRankLadderInfoHeader() {
		
        String header = String.format( 
        		"&d%-12s   %16s   %5s   %-12s   %-12s",
        		"Ladder",
        		"Rank Cost Mult",
        		"Ranks",
        		"First Rank",
        		"Last Rank"
        		);
        
        return header;
	}
	
	public String printRankLadderInfoDetail( RankLadder ladder ) {
		
		DecimalFormat dFmt = Prison.get().getDecimalFormat( "#,##0.0000" );
		
		int rankCount = ladder.getRanks() == null ? 0 : ladder.getRanks().size();
    	
    	Rank firstRank = rankCount == 0 ? null : ladder.getRanks().get(0);
    	Rank lastRank = rankCount == 0 ? null : ladder.getRanks().get( rankCount - 1 );
    	
    	String ladderInfo = String.format(
    			"&7%-12s   %16s   %5d   %-12s   %-12s", 
    			ladder.getName(),
    			dFmt.format( ladder.getRankCostMultiplierPerRank() ),
    			rankCount,
    			(firstRank == null ? "" : firstRank.getName()),
    			(lastRank == null ? "" : lastRank.getName())
    			);
		
		return ladderInfo;
	}

	public String getLadderByFileName(String fileName) {
		String results = "";
		
		for (RankLadder rankLadder : loadedLadders) {
			
			// NOTE: if the ladder was renamed, will need to check the old 
			//       name? 
			
			// Check using the new file name for the ladder:
			String ladderFileName = getLadderNameNew(rankLadder) + ".json";
			if ( ladderFileName.equalsIgnoreCase(fileName) ) {
				results = rankLadder.getName();
			}
			else {
				
				// Check using the old file name for the ladder:
				String ladderFileName2 = getLadderNameOld(rankLadder) + ".json";
				if ( ladderFileName2.equalsIgnoreCase(fileName) ) {
					results = rankLadder.getName();
				}
			}
		}
		return results;
	}
	
    /**
     * A default ladder is absolutely necessary on the server, so let's create it if it doesn't exist, this also create the prestiges ladder.
     */
    public void createDefaultLadder() {
        if ( getLadder(LadderManager.LADDER_DEFAULT) == null ) {
            RankLadder rankLadder = createLadder(LadderManager.LADDER_DEFAULT);

            if ( rankLadder == null ) {
            	
            	String failureMsg = prisonRanks.prisonRanksFailureCreateDefaultLadderMsg();
            	
            	Output.get().logError( failureMsg );
                prisonRanks.getStatus().toFailed( failureMsg );
                return;
            }

            if ( !save( rankLadder ) ) {
            	
            	String failureMsg = prisonRanks.prisonRanksFailureSavingDefaultLadderMsg();
            	
            	Output.get().logError( failureMsg );
                prisonRanks.getStatus().toFailed( failureMsg );
            }
        }

        if ( getLadder(LadderManager.LADDER_PRESTIGES) == null ) {
            RankLadder rankLadder = createLadder(LadderManager.LADDER_PRESTIGES);

            if ( rankLadder == null ) {

            	String failureMsg = prisonRanks.prisonRanksFailureCreatePrestigeLadderMsg();
            	
            	Output.get().logError( failureMsg );
                prisonRanks.getStatus().toFailed( failureMsg );
                return;
            }

            if ( !save( rankLadder ) ) {

            	String failureMsg = prisonRanks.prisonRanksFailureSavingPrestigeLadderMsg();
            	
            	Output.get().logError( failureMsg );
                prisonRanks.getStatus().toFailed( failureMsg );
            }
        }

    }


	
	public List<RankLadder> getLoadedLadders() {
		return loadedLadders;
	}
	public void setLoadedLadders(List<RankLadder> loadedLadders) {
		this.loadedLadders = loadedLadders;
	}

	
	private void resetAllLadders() {
		
		this.loadedLadders = new ArrayList<>();
	}
	
	public void reloadAllLadders( RankManager rankManager ) 
			throws IOException {
		Output.get().logInfo( "Ranks: Loading Ladders..." );

		resetAllLadders();
		
		loadLadders( rankManager );

		
        Output.get().logInfo( "Ranks: Finished Loading Ladders." );

	}
	
}
