package net.minecraft.src;

import net.minecraft.client.Minecraft;
import java.io.*;
import java.util.*;
import org.lwjgl.opengl.GL11;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Durability Warning mod for Minecraft.
 * Requires a hook into one of Minecraft's classes that has a reference to the main Minecraft instance, like PlayerControllerMP
 * @author	Sean Dohring
 */
public class DurabilityWarning{
	
	public final String dw_version = "1.4.6";
	
	public boolean dw_wood;
	public boolean dw_stone;
	public boolean dw_iron;
	public boolean dw_gold;
	public boolean dw_diamond;
	public boolean dw_multiline;
	public String dw_warnings;
	public String last_warning;
	public String warning_tool;
	public Minecraft mc;
	private String[] codes;
	private int[] colours;
	
	/**
	 * @param {net.minecraft.client.Minecraft} minecraft
	 */
	public DurabilityWarning(Minecraft minecraft) {
		mc = minecraft;
		dw_wood = false;
		dw_stone = false;
		dw_iron = false;
		dw_gold = false;
		dw_diamond = false;
		dw_multiline = false;
		dw_warnings = "";
		warning_tool = "";
		last_warning = "";
		codes = new String[16];
		for(int i = 0; i < 16; i++){
			if(i < 10) codes[i] = Character.toString((char)(48 + i));
			else codes[i] = Character.toString((char)(97 + (i - 10)));
		}
		colours = new int[6];
		colours[0] = 15;
		colours[1] = 15;
		colours[2] = 15;
		colours[3] = 15;
		colours[4] = 15;
		colours[5] = 15;
		durabilityWarningSetup(dw_version);
	}
	
	/**
	 * Wrapper method for adding a line to the chat.
	 * Useful because Mojang seems to like changing their chat classes a lot.
	 * @param {String} message The line to add to chat
	 */
	private void addChatMessage(String message) {
		mc.ingameGUI.getChatGUI().printChatMessage(message);
	}
	
	/**
	 * Main method. Call to check if a warning needs to be printed.
	 */
	public void durabilityWarning(){
		ItemStack itemstack = mc.thePlayer.getCurrentEquippedItem();
		// empty hand
		if(itemstack == null) {
			return;
		}
		
		String itemname = itemstack.getItem().getItemName();
		if ((itemstack.isItemStackDamageable()) && ((itemstack.getMaxDamage() != 32) || (itemname.toLowerCase().indexOf("gold") >= 0))) {
			int damage = itemstack.getItemDamage();
			int durability = itemstack.getMaxDamage();
			
			itemname = itemname.substring(5);
			for (int a = 0; a < itemname.length(); a++) {
				if (Character.isUpperCase(itemname.charAt(a))) {
					itemname = itemname.substring(a)+" "+itemname.substring(0, a);
					break;
				}
			}
			
			boolean warn = false;
			
			if ((itemname.toLowerCase().indexOf("wood") >= 0) && (dw_wood == true)){
				warn = true;
			}else if ((itemname.toLowerCase().indexOf("stone") >= 0) && (dw_stone == true)){
				warn = true;
			}else if ((itemname.toLowerCase().indexOf("iron") >= 0) && (dw_iron == true)){
				warn = true;
			}else if ((itemname.toLowerCase().indexOf("gold") >= 0) && (dw_gold == true)){
				warn = true;
			}else if ((itemname.toLowerCase().indexOf("diamond") >= 0) && (dw_diamond == true)){
				warn = true;
			}
			
			if (warn == true) {
				warn = false;
				int hits_remaining = durability - damage;
				int perc_durability = (int)Math.ceil(100 * hits_remaining / durability);
				int last_durability = (int)Math.ceil(100 * (hits_remaining + 1) / durability);
				String[] dw_arr = dw_warnings.split(",");
				for (int i = 0; i < dw_arr.length; i++) {
					boolean perc = false;
					if (dw_arr[i].indexOf("%") >= 0) {
						perc = true;
					}
					String value = dw_arr[i].replace("%", "");
					if (dw_arr[i].indexOf("-") >= 0) {
						int start = Integer.parseInt(value.split("-")[0]);
						int end = Integer.parseInt(value.split("-")[1]);
						if (end > start) {
							int temp = end;
							end = start;
							start = temp;
						}
						if ((perc) && (start >= perc_durability) && (end <= perc_durability))
							warn = true;
						else if ((!perc) && (start >= hits_remaining) && (end <= hits_remaining)) {
							warn = true;
						}
					}
					else if ((perc) && (Integer.parseInt(value) == perc_durability) && (Integer.parseInt(value) != last_durability)) {
						warn = true;
					} else if ((perc) && (Integer.parseInt(value) >= perc_durability) && (Integer.parseInt(value) <= last_durability)) {
						warn = true;
					} else if ((!perc) && (Integer.parseInt(value) == hits_remaining)) {
						warn = true;
					}
					
					if ((damage != 0) && (perc == true) && (perc_durability == last_durability)) {
						warn = false;
					}
					
					if (warn == true) {
						warn = false;
						
						if (dw_multiline) addChatMessage("\247"+codes[colours[3]]+"=========================[DW]=========================");
						if (perc)
							addChatMessage("\247"+codes[colours[4]]+(!dw_multiline ? "=[DW]=" : "")+itemname+" is at "+perc_durability+"% durability"+(!dw_multiline ? "===" : ""));
						else {
							addChatMessage("\247"+codes[colours[4]]+(!dw_multiline ? "=[DW]=" : "")+itemname+" has "+hits_remaining+" hits left"+(!dw_multiline ? "===" : ""));
						}
						if (dw_multiline) addChatMessage("\247"+codes[colours[5]]+"=====================================================");
						
						last_warning = value+(perc ? "%" : "");
						warning_tool = itemname;
						break;
					}
				}
			}
		}
	}
	
	/**
	 * Setup method. Called once when loading a world/server.
	 * @param {String} v Mod version to be printed
	 */
	private void durabilityWarningSetup(String v){
		
		String userOS = System.getProperty("os.name").toUpperCase();
		String saveDir = "";
		if (userOS.indexOf("WIN") >= 0)
			saveDir = new StringBuilder().append(getAppData()).append("\\.minecraft\\").toString();
		else if (userOS.indexOf("MAC") >= 0)
			saveDir = new StringBuilder().append(System.getProperty("user.home")).append("/Library/Application Support/minecraft/").toString();
		else if ((userOS.indexOf("NIX") >= 0) || (userOS.indexOf("NUX") >= 0)) {
			saveDir = new StringBuilder().append(System.getProperty("user.home")).append("/.minecraft/").toString();
		}
		
		File f = new File(new StringBuilder().append(saveDir).append("durabilitywarning.txt").toString());
		if (!f.exists()) {
			try {
				f.createNewFile();
				try
				{
					FileWriter fstream = new FileWriter(saveDir+"durabilitywarning.txt");
					BufferedWriter out = new BufferedWriter(fstream);
					out.write("# show warnings for which tool types\nwood: false\nstone: true\niron: true\ngold: true\ndiamond: true\n\n");
					out.write("# show the 3-line warning?\nmultiline: true\n\n");
					out.write("# when to show warnings\nwarnings: 10 - 0, 50%, 100% - 99%\n\n");
					out.write("# receive a notification when mod is updated?\nupdatenotify: true");
					out.close();
				}
				catch (IOException exp) {
					addChatMessage(">>[DW]>> "+exp.getMessage());
				}
			} catch (IOException exp) {
				addChatMessage(">>[DW]>> "+exp.getMessage());
			}
			
		}
		
		boolean checkversion = true;
		try {
			FileInputStream fstream = new FileInputStream(saveDir+"durabilitywarning.txt");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			
			dw_warnings = "";
			String strLine;
			while ((strLine = br.readLine()) != null) {
				if ((strLine.indexOf("#") >= 0) || (strLine.indexOf(":") < 0)) {
					continue;
				}
				String property = strLine.substring(0, strLine.indexOf(":"));
				String value = strLine.substring(strLine.indexOf(":") + 1);
				
				if (property.indexOf("wood") >= 0) {
					dw_wood = (value.indexOf("true") >= 0);
					
				}else if (property.indexOf("stone") >= 0) {
					dw_stone = (value.indexOf("true") >= 0);
					
				}else if (property.indexOf("iron") >= 0) {
					dw_iron = (value.indexOf("true") >= 0);
					
				}else if (property.indexOf("gold") >= 0) {
					dw_gold = (value.indexOf("true") >= 0);
					
				}else if (property.indexOf("diamond") >= 0) {
					dw_diamond = (value.indexOf("true") >= 0);
					
				}else if (property.indexOf("multiline") >= 0) {
					dw_multiline = (value.indexOf("true") >= 0);
					
				}else if (property.indexOf("warnings") >= 0) {
					value = value.replace(" ", "");
					value = value.replace("\n", "");
					String[] valuearr = value.split(",");
					for (int i = 0; i < valuearr.length; i++) {
						if (valuearr[i].indexOf("-") >= 0) {
							String currentvalue = valuearr[i].replace("%", "");
							if (valuearr[i].indexOf("%") >= 0){
								dw_warnings = dw_warnings+"%"+currentvalue;
							} else {
								dw_warnings = dw_warnings+currentvalue;
							}
						}
						else if (valuearr[i].indexOf("%") >= 0) {
							dw_warnings = dw_warnings+"%"+valuearr[i].replace("%", "");
						} else {
							dw_warnings = dw_warnings+valuearr[i];
						}
						
						if (i + 1 < valuearr.length)
							dw_warnings = dw_warnings+",";
					}
				}else if (property.indexOf("updatenotify") >= 0){
					if(value.indexOf("false") >= 0) {
						checkversion = false;
					}
				}else if (property.indexOf("welcome") >= 0){
					colours[0] = Integer.parseInt(value.trim());
					
				}else if (property.indexOf("loaded") >= 0){
					colours[1] = Integer.parseInt(value.trim());
					
				}else if (property.indexOf("update") >= 0){
					colours[2] = Integer.parseInt(value.trim());
					
				}else if (property.indexOf("top") >= 0){
					colours[3] = Integer.parseInt(value.trim());
					
				}else if (property.indexOf("main") >= 0){
					colours[4] = Integer.parseInt(value.trim());
					
				}else if (property.indexOf("bottom") >= 0){
					colours[5] = Integer.parseInt(value.trim());
					
				}
				
			}
			
			addChatMessage("\247"+codes[colours[0]]+"=[DW]= Durability Warning v"+v+" ===");
			if (checkversion)
				try
			{
				java.net.URL vurl = new java.net.URL("http://dl.dropbox.com/u/2601726/dw.txt");
				try {
					InputStream is = vurl.openStream();
					String newest = new Scanner(is).useDelimiter("\\A").next();
					if (!newest.equals(v))
						addChatMessage("\247"+codes[colours[2]]+"=== There is a newer version ("+newest+") of DW available ===");
				} catch (IOException e) {
				}
			} catch (java.net.MalformedURLException e) {
			}
			addChatMessage("\247"+codes[colours[1]]+"=== "+dw_warnings.split(",").length+" warnings loaded ===");
			in.close();
			
		}
		catch (Exception e) {
			addChatMessage(">> DW_Error >> "+e.getMessage());
		}
	}
	
	private static String getAppData()
	{
		ProcessBuilder builder = new ProcessBuilder(new String[] { "cmd", "/C echo %APPDATA%" });
		
		BufferedReader br = null;
		try {
			Process start = builder.start();
			br = new BufferedReader(new InputStreamReader(start.getInputStream()));
			String path = br.readLine();
			
			if (path.endsWith("\"")) {
				path = path.substring(0, path.length() - 1);
			}
			String str1 = path.trim();
			return str1;
		}
		catch (IOException ex)
		{
			// Not sure how to handle this. Absorb for now.
		}
		
		return null;
	}
	
}
