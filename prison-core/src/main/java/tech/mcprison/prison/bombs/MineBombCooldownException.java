package tech.mcprison.prison.bombs;

import tech.mcprison.prison.Prison;

public class MineBombCooldownException 
	extends Exception {

	private static final long serialVersionUID = 1L;

	private int cooldownTicks;
	
	public MineBombCooldownException( int cooldownTicks ) {
		super( "Bomb cooldown is in effect. Try again later. Seconds remaining: " + 
				Prison.getDecimalFormatStaticDouble().format( cooldownTicks / 20.0d )
				);
		
		this.cooldownTicks = cooldownTicks;
	}

	public String getCooldownSecondsFormatted() {
		String results = "";
		
		double seconds = getCooldownTicks() / 20.0d;
		
		results = Prison.getDecimalFormatStaticDouble().format( seconds );
		
		return results;
	}
	
	public int getCooldownTicks() {
		return cooldownTicks;
	}
	public void setCooldownTicks(int cooldownTicks) {
		this.cooldownTicks = cooldownTicks;
	}
	
}
