package tech.mcprison.prison.spigot.block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.ItemMeta;

import com.cryptomorin.xseries.XMaterial;

import tech.mcprison.prison.Prison;
import tech.mcprison.prison.internal.ItemStack;
import tech.mcprison.prison.internal.block.PrisonBlock;
import tech.mcprison.prison.spigot.SpigotUtil;
import tech.mcprison.prison.spigot.compat.SpigotCompatibility;
import tech.mcprison.prison.spigot.nbt.PrisonNBTUtil;
import tech.mcprison.prison.util.Text;

public class SpigotItemStack
		extends ItemStack {

	private org.bukkit.inventory.ItemStack bukkitStack;
//	private NBTItem nbtBukkitStack;
//	private boolean nbtChecked = false;
//	private org.bukkit.inventory.ItemStack bukkitStack;
//	private org.bukkit.inventory.ItemStack deserialize;
	
	public SpigotItemStack( org.bukkit.inventory.ItemStack bukkitStack )
		throws PrisonItemStackNotSupportedRuntimeException {
		super();

		this.bukkitStack = bukkitStack;
//		this.nbtBukkitStack = null;
		
		setupBukkitStack( bukkitStack );
	}
	
	private void setupBukkitStack( org.bukkit.inventory.ItemStack bukkitStack ) {
		XMaterial xMat = null;
		
		if ( bukkitStack != null ) {
			
			try {
				xMat = XMaterial.matchXMaterial( bukkitStack );
			} catch (Exception e) {
				
				String message = String.format( 
						"Unsupported ItemStack type: %s",
						e.getMessage() );
				throw new PrisonItemStackNotSupportedRuntimeException( message );
			}
		}
		
//		if ( xMat != XMaterial.AIR ) {
//			
//			NBTItem nbtItemStack = new NBTItem( bukkitStack, true );
//			
//			this.nbtBukkitStack = nbtItemStack;
////		this.bukkitStack = bukkitStack;
//		}
		
		
        if (bukkitStack == null || bukkitStack.getType().equals(Material.AIR)) {
        	  setAmount( 0 );
              setMaterial( PrisonBlock.AIR );
              setDisplayName( "air" );
        }
        else {
        	
        	ItemMeta meta = getMeta();

        	
        	
        	// We are getting the bukkit amount here, but if it changes, we must set the amount
        	// in the bukkitStack:
        	int amount = bukkitStack.getAmount();
        	
        	
        	
        	
//        	BlockType type = SpigotCompatibility.getInstance()
//        			.getBlockType( bukkitStack );
//        BlockType type = materialToBlockType(bukkitStack.getType());
        	
        	
        	String displayName = null;
        	
        	if (meta.hasDisplayName()) {
        		displayName = meta.getDisplayName();
        	}
//        	else if ( type != null ) {
//        		displayName = type.getBlockName().toLowerCase();
//        	}
        	
        	
        	PrisonBlock type = SpigotUtil.getPrisonBlock( xMat, displayName );
        	
        	
        	List<String> lores = new ArrayList<>();
        	
        	if ( meta.hasLore() ) {
        		for ( String lore : meta.getLore() ) {
					lores.add( lore );
				}
        	}
        	
        	setAmount( amount );
        	setMaterial( type );
        	
        	// Update only the prison internals for the displayName and lore:
        	super.setDisplayName( displayName );
        	super.setLore( lores );
        }

		
    }
	

	
    public SpigotItemStack(String displayName, int amount, PrisonBlock material, String... lore) {
        super(displayName, amount, material, lore );
        
        SpigotItemStack sItemStack = SpigotUtil.getSpigotItemStack( material, amount );
        
        this.bukkitStack = sItemStack.getBukkitStack();
        
        if ( bukkitStack != null ) {
        	setupBukkitStack( bukkitStack );
        }
    }

    public SpigotItemStack(int amount, PrisonBlock material, String... lore) {
    	super( amount, material, lore );
    	
    	SpigotItemStack sItemStack = SpigotUtil.getSpigotItemStack( material, amount );
    	
    	this.bukkitStack = sItemStack.getBukkitStack();
    	
    	if ( bukkitStack != null ) {
    		setupBukkitStack( bukkitStack );
    	}
    }
    
    
    
    public SpigotItemStack( ItemStack iStack) {
    	super( iStack );
    	
    	org.bukkit.inventory.ItemStack bStack = 
    			( iStack instanceof SpigotItemStack ?
    					((SpigotItemStack) iStack).getBukkitStack() :
    						null );
    	
    	
    	if ( bStack == null ) {
    		
    		XMaterial xMat =  SpigotCompatibility.getInstance().getXMaterial( getMaterial() );
    		if ( xMat != null ) {
    			bStack = xMat.parseItem();
    		}
    	}
    	
    	if ( bStack != null ) {
    		
    		this.bukkitStack = bStack.clone();
    		
    		if ( bukkitStack != null ) {
    			setupBukkitStack( bukkitStack );
    		}
    	}

    }
    
    
    public void setPrisonBlock( PrisonBlock pBlock ) {
    	
    	String displayName = pBlock.getBlockName();
    	
    	if ( pBlock.getDisplayName() != null ) {
    		displayName = pBlock.getDisplayName();
    	}
    	
    	setDisplayName( displayName );
    	
    	setMaterial( pBlock );
    }
    
    
//    /**
//     * <p>This will check to see if the nbt library is active on this itemStack,
//     * of which there are some items and blocks that it cannot be used with.
//     * If it has not been checked before, it will attempt a check.
//     * </p>
//     * 
//     * @return
//     */
//    public boolean isNBTEnabled() {
//    	
//    	if ( !nbtChecked && bukkitStack != null && bukkitStack.getType() != Material.AIR ) {
//    		
//    		try {
//				NBTItem nbtItemStack = new NBTItem( bukkitStack, true );
//				
//				this.nbtBukkitStack = nbtItemStack;
//			} catch (Exception e) {
//				// ignore - the bukkit item stack is not compatible with the NBT library
//			}
//
//    		this.nbtChecked = true;
//    	}
//    	
//    	return nbtBukkitStack != null;
//    }
    
//    public NBTItem getNBT() {
//    	NBTItem nbtItemStack = null;
//    	
//    	if ( getBukkitStack() != null && getBukkitStack().getType() != Material.AIR  ) {
//    		try {
//				nbtItemStack = new NBTItem( getBukkitStack(), true );
//				
//				nbtDebugLog( nbtItemStack, "getNbt" );
//			} catch (Exception e) {
//				// ignore - the bukkit item stack is not compatible with the NBT library
//			}
//    	}
//    	
//    	return nbtItemStack;
//    }
    
//    private void applyNbt( NBTItem nbtItem ) {
//		if ( nbtItem != null && getBukkitStack() != null ) {
//			
////			nbtItem.applyNBT( getBukkitStack() );
//			
//			nbtDebugLog( nbtItem, "applyNbt" );
//		}
//    }
    
//    private void nbtDebugLog( NBTItem nbtItem, String desc ) {
//		if ( Output.get().isDebug() ) {
//			org.bukkit.inventory.ItemStack iStack = nbtItem.getItem();
//			
//			int sysId = System.identityHashCode(iStack);
//			
//			String message = String.format( 
//					"NBT %s ItemStack for %s: %s  sysId: %d", 
//					desc,
//					iStack.hasItemMeta() && iStack.getItemMeta().hasDisplayName() ? 
//							iStack.getItemMeta().getDisplayName() :
//							iStack.getType().name(),
//					nbtItem.toString(),
//					sysId );
//			
//			Output.get().logInfo( message );
//			
//			Output.get().logInfo( "NBT: " + new NBTItem( getBukkitStack() ) );
//			
//		}
//    }
    
    public boolean hasNBTKey( String key ) {
    	boolean results = PrisonNBTUtil.hasNBTString( getBukkitStack(), key);
    	
//    	NBTItem nbtItem = getNBT();
//    	if ( nbtItem != null ) {
//    		results = nbtItem.hasKey( key );
//    	}
    	
    	return results;
    }
    
    public String getNBTString( String key ) {
    	String results = PrisonNBTUtil.getNBTString( getBukkitStack(), key);
    	
//    	NBTItem nbtItem = getNBT();
//    	if ( nbtItem != null ) {
//    		results = nbtItem.getString( key );
//    	}
    	return results;
    }
    public void setNBTString( String key, String value ) {
    	
    	PrisonNBTUtil.setNBTString( getBukkitStack(), key, value );
    	
//    	NBTItem nbtItem = getNBT();
//    	if ( nbtItem != null ) {
//    		nbtItem.setString( key, value );
//    		nbtDebugLog( nbtItem, "setNBTString" );
//    	}
    }
    
    
    
    public String getNBTItemStackInfo() {
    	
    	String results = PrisonNBTUtil.nbtDebugString(getBukkitStack()) ;
    	
    	return results;
    }
    
//    public int getNBTInt( String key ) {
//    	int results = -1;
//    	
//    	NBTItem nbtItem = getNBT();
//    	if ( nbtItem != null ) {
//     		results = nbtItem.getInteger( key );
//    	}
//    	return results;
//    }
//    public void setNBTInt( String key, int value ) {
//    	
//    	NBTItem nbtItem = getNBT();
//    	if ( nbtItem != null ) {
//     		nbtItem.setInteger( key, value );
//     		nbtDebugLog( nbtItem, "setNBTInt" );
//    	}
//    }
    
//    public double getNBTDouble( String key ) {
//    	double results = -1d;
//    	
//    	NBTItem nbtItem = getNBT();
//    	if ( nbtItem != null ) {
//    		results = nbtItem.getDouble( key );
//    	}
//    	return results;
//    }
//    public void setNBTDouble( String key, double value ) {
//    	
//    	NBTItem nbtItem = getNBT();
//    	if ( nbtItem != null ) {
//    		nbtItem.setDouble( key, value );
//    		nbtDebugLog( nbtItem, "setNBTDouble" );
//    	}
//    }
    
//    public boolean getNBTBoolean( String key ) {
//    	boolean results = false;
//    	
//    	NBTItem nbtItem = getNBT();
//    	if ( nbtItem != null ) {
//    		results = nbtItem.getBoolean( key );
//    	}
//    	return results;
//    }
//    public void setNBTBoolean( String key, boolean value ) {
//    	
//    	NBTItem nbtItem = getNBT();
//    	if ( nbtItem != null ) {
//    		nbtItem.setBoolean( key, value );
//    		nbtDebugLog( nbtItem, "setNBTBoolean" );
//    	}
//    }
    
    
//    	
//    public void setNbtString( org.bukkit.inventory.ItemStack bItemStack, String key, String value ) {
//    	NBTItem nbt = new NBTItem( bItemStack );
//    	nbt.setString( key, value );
//    	nbt.applyNBT( bItemStack );
//    }
//    	
//    public String getNbtValue( org.bukkit.inventory.ItemStack bItemStack, String key ) {
//    	NBTItem nbt = new NBTItem( bItemStack );
//    	return nbt.getString( key );
//    }
//    
    
	/**
	 * <p>This function overrides the Prison's ItemStack class's setAmount() to perform the 
	 * same behavior of setting the amount to the requested value, but it also updates
	 * the bukkitStack's amount to ensure that it is the correct value.
	 * </p>
	 * 
	 */
	@Override
	public void setAmount( int amount ) {
		super.setAmount( amount );
		
		if ( bukkitStack != null && bukkitStack.getAmount() != amount ) {
			bukkitStack.setAmount( amount );
		}
	}
	
	/**
	 * <p>This function will add the given amount to the item stack's total amount.
	 * </p>
	 * 
	 * @param i amount to add to this itemStack
	 */
	public void addToAmount( int i ) {
		int amt = getAmount() + i;
		setAmount( amt );
	}
	
	
	private ItemMeta getMeta() {
		ItemMeta meta = null;
			
		if ( getBukkitStack() != null ) {
			if (!getBukkitStack().hasItemMeta()) {
				meta = Bukkit.getItemFactory().getItemMeta(getBukkitStack().getType());
			} else {
				meta = getBukkitStack().getItemMeta();
			}
		}
		
		return meta;
	}
	
	@Override
	public void setDisplayName( String displayName ) {
		
		if ( getBukkitStack() != null ) {
			
			ItemMeta meta = getMeta();
			if ( meta != null && displayName != null && displayName.trim().length() > 0 ) {
				
				meta.setDisplayName( Text.translateAmpColorCodes(displayName) );
				
				getBukkitStack().setItemMeta( meta );
			}
		}
		
		super.setDisplayName( displayName );
	}
	
	@Override
	public void setLore( List<String> lores ) {
		
		if ( getBukkitStack() != null ) {
			
			ItemMeta meta = getMeta();
			if ( meta != null && lores != null && lores.size() > 0 ) {
				
				List<String> updatedLores = new ArrayList<>();
				
				for ( String lore : lores ) {
					updatedLores.add( Text.translateAmpColorCodes(lore) );
				}
				
				meta.setLore( updatedLores );
				
				getBukkitStack().setItemMeta( meta );
				
				getLore().addAll( updatedLores );
				
			}
		}
		
		//super.setLore( updatedLores );
	}
	
	
	public boolean isAir() {
		boolean results = false;
		
		if ( getMaterial() != null && getMaterial().isAir() ||
				getName() != null && "air".equalsIgnoreCase( getName() ) ) {
			results = true;
		}
		else if ( getBukkitStack() != null ) {
			results = getBukkitStack().getType().equals( Material.AIR );
		}
		
		return results;
	}
	
	public boolean isBlock() {
		boolean results = false;
		
		if ( getBukkitStack() != null ) {
			results = getBukkitStack().getType().isBlock();
//			XMaterial xMat = XMaterial.matchXMaterial( getBukkitStack() );
//			if ( xMat != null ) {
//				results = xMat.parseMaterial().isBlock();
//			}
		}
		
		return results;
	}
	

    
	public org.bukkit.inventory.ItemStack getBukkitStack() {
		return bukkitStack;
	}
	public void setBukkitStack( org.bukkit.inventory.ItemStack bukkitStack ) {
		
		this.bukkitStack = bukkitStack;
//		this.nbtBukkitStack = null;
		
		setupBukkitStack( bukkitStack );
	}

	
//	public NBTItem getNbtBukkitStack() {
//		return nbtBukkitStack;
//	}




	public Map<String, Object> serialize()
	{
		Map<String, Object> results = new HashMap<>();
		
		if ( getBukkitStack() != null ) {
			results = getBukkitStack().serialize();
		}
		
		results.put( "prison_version", Prison.get().getPlatform().getPluginVersion() );
		results.put( "isAir", isAir() );
		results.put( "isBlock", isBlock() );
		
		return results;
	}
	
	public static SpigotItemStack deserialize( Map<String, Object> map )
	{
//		-String prisonVersion = (String) map.get( "prison_version" );
		
		org.bukkit.inventory.ItemStack iStack = org.bukkit.inventory.ItemStack.deserialize(map);
		
		SpigotItemStack sItemStack = new SpigotItemStack( iStack );
				
		return sItemStack;
	}
	
	public Map<Enchantment, Integer> getEnchantments() {
		Map<Enchantment, Integer> results = null;
		
		ItemMeta meta = getMeta();
		if ( meta != null && 
				meta.getEnchants() != null &&
				meta.getEnchants().size() > 0 ) {
			
			results = meta.getEnchants();
		}
        return results;
    }

	/**
	 * <p>This function will return information on the item in the item stack, which is for 
	 * debugging purposes, such as displayed within the autoFeatures debug info.
	 * </p>
	 * 
	 * @return
	 */
	public String getDebugInfo() {
		StringBuilder sb = new StringBuilder();
		
		sb.append( getName() );

		if ( getBukkitStack() != null ) {
			
			ItemMeta meta = getMeta();
			if ( meta != null && 
					meta.getEnchants() != null &&
					meta.getEnchants().size() > 0 ) {
				sb.append( " " );
				
				StringBuilder sbE = new StringBuilder();
				Set<Enchantment> keys = meta.getEnchants().keySet();
				for (Enchantment key : keys) {
					if ( sbE.length() > 0 ) {
						sbE.append(",");
					}
					String name = key.toString();
					name = name.substring(name.indexOf(" ") + 1, name.length() - 1);
					
					Integer level = meta.getEnchants().get(key);
					sbE.append( name );
					sbE.append(":");
					sbE.append( level );
				}
				
				if ( sbE.length() > 0 ) {
					sb.append("(");
					sb.append( sbE );
					sb.append(")");
				}
			}
		}
		
		if ( SpigotCompatibility.getInstance().hasDurability( this ) ) {
			int durabilityMax = SpigotCompatibility.getInstance().getDurabilityMax( this );
			int durability = SpigotCompatibility.getInstance().getDurability( this );
			
			sb.append(" durability:");
			sb.append(durabilityMax);
			sb.append(":");
			sb.append(durability);
		}
		
		if ( getAmount() != 1 ) {
			sb.append( " amount=" );
			sb.append( getAmount() );
		}
			
		sb.append( " " );
		if ( isAir() ) {
			sb.append( "::AIR" );
		}
		else if ( isBlock() ) {
			sb.append( "::BLOCK" );
		}
		
		return sb.toString();
	}
}
