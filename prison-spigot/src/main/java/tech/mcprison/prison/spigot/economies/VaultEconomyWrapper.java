package tech.mcprison.prison.spigot.economies;

import java.text.DecimalFormat;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.economy.EconomyResponse;
import tech.mcprison.prison.Prison;
import tech.mcprison.prison.internal.Player;
import tech.mcprison.prison.output.Output;
import tech.mcprison.prison.spigot.SpigotUtil;
import tech.mcprison.prison.spigot.spiget.BluesSpigetSemVerComparator;

/**
 * <p>Prison does not support banks since the only way to identify a bank is through
 * the player's name.  Not even sure if it was implemented correctly in the past.
 * If there is a need to reenable this code, then bank support can be added
 * back in the future, and at that time, it can be verified to be correct.
 * I honestly don't think it is correct the way it was being used before.
 * </p>
 *
 */
public class VaultEconomyWrapper
{
	private net.milkbowl.vault.economy.Economy economy = null;
	
	private String providerName;
	private String vaultVersion;
	
	private boolean preV1dot4 = false;
	
	private final HashSet<Player> hasNoAccount;
	
	public VaultEconomyWrapper(String providerName ) {
		super();
		
		this.hasNoAccount = new HashSet<>();
		
		this.providerName = providerName;
		
		RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> economyProvider =
				Bukkit.getServer().getServicesManager()
				.getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
			
			
			this.vaultVersion = 
					Bukkit.getPluginManager().getPlugin( getProviderName() )
								.getDescription().getVersion();
			
			this.preV1dot4 = ( new BluesSpigetSemVerComparator().compareTo( getVaultVersion(), 
									"1.4.0" ) < 0 );
			
//			Output.get().logInfo( "### VaultEconomyWrapper : vaultVersion = " + getVaultVersion() + 
//					"  is pre1_4= " + isPreV1_4() );
		}
	}
	
	/**
	 * <p>Checks if the player has an account, if they do not, then they are 
	 * added to the HashSet and the informational message about no account
	 * will only be shown once.
	 * </p>
	 * 
	 * @param player
	 * @return
	 */
	public boolean hasAccount( Player player, boolean sendWarning ) {
		boolean hasAccount = false;
		
		OfflinePlayer oPlayer = getOfflinePlayer( player );
		
		try {
			hasAccount = economy.hasAccount( oPlayer );
		} 
		catch (Exception e) {
			
			if ( sendWarning && !hasNoAccount.contains(player) ) {
				Output.get().logInfo( 
						String.format( 
								"Vault economy: Player %s does not have an econ account. "
										+ "Informational. Not a concern. Message: [%s]",
										player.getName(), e.getMessage()
								));
				
				hasNoAccount.add(player);
			}
		}
		
    	
    	return hasAccount;
	}
	
	public String getProviderName() {
		return providerName;
	}
	public void setProviderName( String providerName ) {
		this.providerName = providerName;
	}

	public String getVaultVersion() {
		return vaultVersion;
	}
	public void setVaultVersion( String vaultVersion ) {
		this.vaultVersion = vaultVersion;
	}

	public boolean isPreV1_4() {
		return preV1dot4;
	}
	public void setPreV1_4( boolean preV1_4 ) {
		this.preV1dot4 = preV1_4;
	}


	public boolean isEnabled()
	{
		return economy != null && economy.isEnabled();
	}

	public String getName() {
		return economy == null ? "not enabled" : economy.getName();
	}
	
	private OfflinePlayer getOfflinePlayer(Player player) {
		
		return SpigotUtil.getBukkitOfflinePlayer( player.getUUID() );
	}
	
	
	/**
	 * <p>This gets a player's balance.
	 * </p>
	 * 
	 * <p>Note that if OfflinePlayer is null, then it appears that this is a 
	 * symptom of using a plugin, such as Citizens, and that a NPC is 
	 * running a prison command.  So a null OfflinePlayer is not a sign
	 * of failure, as much as a sign of usage of an NPC.  So ignore 
	 * these conditions and no longer print an error message.
	 * </p>
	 * 
	 * @param player
	 * @return
	 */
    @SuppressWarnings( "deprecation" )
	public double getBalance(Player player) {
    	double results = 0;
 
    	boolean hasAccount = hasAccount(player, true);
    	
    	if ( economy != null && !hasAccount ) {
    		player.sendMessage( "Economy Error: You don't have an account.");
    	}
    	else 
        if (economy != null) {
        	
        	if ( isPreV1_4() ) {
        		results = economy.getBalance(player.getName());
        	}
        	else {
        		OfflinePlayer oPlayer = getOfflinePlayer( player );
        		if ( oPlayer != null ) {
        			results = economy.getBalance(oPlayer);
        		}
        		
        		
//        		if ( oPlayer == null ) {
//        			Output.get().logInfo( "VaultEconomyWrapper.getBalance(): Error: " +
//        					"Cannot get economy for player %s so returning a value of 0. " +
//        					"Failed to get an bukkit offline player.", 
//        					player.getName());
//        			results = 0;
//        		}
//        		else {
//        			results = economy.getBalance(oPlayer);
//        		}
        		
        	}
//        	if ( economy.hasBankSupport() ) {
//
//        		EconomyResponse bnkBal = economy.bankBalance( player.getName() );
//        		if ( bnkBal.transactionSuccess() ) {
//        			results = bnkBal.balance;
//        		}
//        			
//        		
//        	} else {
//        	results = economy.getBalance(player.getName());
//        	}
        }
        
        return results;
    }

    @SuppressWarnings( "deprecation" )
	public boolean setBalance(Player player, double amount) {
    	boolean results = false;

    	boolean hasAccount = hasAccount(player, true);

    	if ( economy != null && !hasAccount ) {
    		player.sendMessage( "Economy Error: You don't have an account.");
    	}
    	else 
        if (economy != null) {
        	
           	if ( isPreV1_4() ) {
           		economy.withdrawPlayer( player.getName(), getBalance( player ) );
           		economy.depositPlayer( player.getName(), amount );
           		results = true;
        	}
        	else {
        		OfflinePlayer oPlayer = getOfflinePlayer( player );
        		if ( oPlayer != null ) {
//        			Output.get().logInfo( "VaultEconomyWrapper.setBalance(): Error: " +
//        					"Cannot get economy for player %s so cannot set balance to %s.", 
//        					player.getName(), Double.toString( amount ));
//        		}
//        		else {
        			economy.withdrawPlayer( oPlayer, getBalance( player ) );
        			economy.depositPlayer( oPlayer, amount );
        			results = true;
        		}
        	}
        	
//        	if ( economy.hasBankSupport() ) {
//        		economy.bankWithdraw(player.getName(), getBalance(player));
//        		economy.bankDeposit(player.getName(), amount);
//        	} else {
//        		economy.withdrawPlayer( player.getName(), getBalance( player ) );
//        		economy.depositPlayer( player.getName(), amount );
//        	}
        }
        return results;
    }

    @SuppressWarnings( "deprecation" )
	public boolean addBalance(Player player, double amount) {
    	boolean results = false;
    	
    	boolean hasAccount = hasAccount(player, true);
    	
    	if ( amount < 0 ) {
    		results = removeBalance( player, amount );
    	}
    	
    	
    	else if ( economy != null && !hasAccount ) {
    		player.sendMessage( "Economy Error: You don't have an account.");
    	}
    	else if (economy != null) {
        	if ( isPreV1_4() ) {
        		economy.depositPlayer( player.getName(), amount );
           		results = true;
        	}
        	else {
        		OfflinePlayer oPlayer = getOfflinePlayer( player );
        		if ( oPlayer != null ) {
//        			Output.get().logInfo( "VaultEconomyWrapper.addBalance(): Error: " +
//        					"Cannot get economy for player %s so cannot add a balance of %s.", 
//        					player.getName(), Double.toString( amount ));
//        		}
//        		else {
        			EconomyResponse response = economy.depositPlayer( oPlayer, amount );

        			results = response.transactionSuccess();
        			
        			if ( !results ) {
        				DecimalFormat dFmt = Prison.get().getDecimalFormat("#,##0.00");
        				String message = String.format( 
        						"VaultEconomy.addBalance failed: %s  amount: %s  " +
        									"balance: %s  error: %s", 
        						oPlayer.getName(),
        						dFmt.format(amount), dFmt.format(response.balance),
        						response.errorMessage );
        				Output.get().logError( message );
        			}
//        			results = true;
        		}
        	}
//        	if ( economy.hasBankSupport() ) {
//        		economy.bankDeposit(player.getName(), amount);
//        	} else {
//        		economy.depositPlayer( player.getName(), amount );
//        	}
        }
        return results;
    }

    @SuppressWarnings( "deprecation" )
	public boolean removeBalance(Player player, double amount) {
    	boolean results = false;
    	
    	// Needs to be a positive amount:
    	if ( amount < 0 ) {
    		amount *= -1;
    	}
    	
    	boolean hasAccount = hasAccount(player, true);

    	if ( economy != null && !hasAccount ) {
    		player.sendMessage( "Economy Error: You don't have an account.");
    	}
    	else 
    	if (economy != null) {
    		if ( isPreV1_4() ) {
    			economy.withdrawPlayer( player.getName(), amount );
    			results = true;
    		}
    		else {
    			OfflinePlayer oPlayer = getOfflinePlayer( player );
    			if ( oPlayer != null ) {
//    				Output.get().logInfo( "VaultEconomyWrapper.removeBalance(): Error: " +
//    						"Cannot get economy for player %s so cannot remove a balance of %s.", 
//    						player.getName(), Double.toString( amount ));
//    			}
//    			else {
    				EconomyResponse response = economy.withdrawPlayer( oPlayer, amount );

    				results = response.transactionSuccess();
        			
        			if ( !results ) {
        				DecimalFormat dFmt = Prison.get().getDecimalFormat("#,##0.00");
        				String message = String.format( 
        						"VaultEconomy.removeBalance failed: %s  amount: %s  " +
        									"balance: %s  error: %s", 
        						oPlayer.getName(),
        						dFmt.format(amount), dFmt.format(response.balance),
        						response.errorMessage );
        				Output.get().logError( message );
        			}
//    				results = true;
    			}
    		}

//        if (economy != null) {
//        	if ( economy.hasBankSupport() ) {
//        		economy.bankWithdraw(player.getName(), amount);
//        	} else {
//        		economy.withdrawPlayer( player.getName(), amount );
//        	}
//        }
    	}
    	return results;
    }

    @SuppressWarnings( "deprecation" )
	public boolean canAfford(Player player, double amount) {
    	boolean results = false;
    	
    	boolean hasAccount = hasAccount(player, true);

    	if ( economy != null && !hasAccount ) {
    		player.sendMessage( "Economy Error: You don't have an account.");
    	}
    	else 
    	if (economy != null) {
       		if ( isPreV1_4() ) {
       			results = economy.has(player.getName(), amount);
    		}
    		else {
    			OfflinePlayer oPlayer = getOfflinePlayer( player );
    			if ( oPlayer != null ) {
//    				Output.get().logInfo( "VaultEconomyWrapper.canAfford(): Error: " +
//    						"Cannot get economy for player %s so cannot tell if " +
//    						"player can afford the amount of %s.", 
//    						player.getName(), Double.toString( amount ));
//    			}
//    			else {
    				results = economy.has(oPlayer, amount);
    			}
    		}

//        	if ( economy.hasBankSupport() ) {
//        		results = economy.bankHas(player.getName(), amount).transactionSuccess();
//        	} else {
//        		results = economy.has(player.getName(), amount);
//        	}
    	}
        return results;
    }

}
