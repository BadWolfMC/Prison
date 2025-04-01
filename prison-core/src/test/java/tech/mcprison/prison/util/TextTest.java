package tech.mcprison.prison.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TextTest
		extends Text
{

	@Test
	public void testStripColor() {
		//
//	    public static final Pattern STRIP_COLOR_PATTERN =
//	            Pattern.compile("(?i)" + COLOR_ + "#[A-Fa-f0-9]{6}|" + 
//	            						 COLOR_ + "[0-9A-FK-OR]");
	    
		assertEquals("This is a test", stripColor("This is a test"));
		assertEquals("This is a test", stripColor("This &7is a test"));
		assertEquals("This is a test", stripColor("This &7is &Ra test"));
		assertEquals("This is a test", stripColor("This &7is &ra test"));
		
		assertEquals("This is a test", stripColor("This &0&1&2&3&4&5&6&7&8&9" +
				"&a&A&B&b&c&C&d&D&e&E&f&F&k&K&l&L&m&M&n&N&o&O&r&Ris &Ra test"));
		
		assertEquals("This is a test", stripColor("&KTh&ris &7is &aa &At&Be&8s&9t"));
		
		assertEquals("This is a test", stripColor("This #123456is &ra test"));
		assertEquals("This is a test", stripColor("This #abCDeFis &ra test"));
		
		
	}

	public void testTranslateAmpColorCodes() {
		assertEquals("This is a test", translateColorCodes("This is a test", 'x'));
		assertEquals("This x7is a test", translateColorCodes("This &7is a test", 'x'));
		assertEquals("This x7is xRa test", translateColorCodes("This &7is &Ra test", 'x'));
		assertEquals("This x7is xra test", translateColorCodes("This &7is &ra test", 'x'));
		
		
		assertEquals("xKThxris x7is xaa xAtxBex8sx9t", 
							translateColorCodes("&KTh&ris &7is &aa &At&Be&8s&9t", 'x'));
		
		assertEquals("This x#123456is xra test", translateColorCodes("This &#123456is &ra test", 'x'));
		assertEquals("This x#abCDeFis &xra test", translateColorCodes("This &#abCDeFis &ra test", 'x'));

		
	}
	
	@Test
	public void testTranslateColorCodesQuotedText() {
		
		// Test no translations:
		assertEquals("This is a test", replaceColorCodeWithx( 
												translateColorCodes("This is a test", '&')));
		
		// test simple translations:
		assertEquals("This x7is a test", replaceColorCodeWithx( 
												translateColorCodes("This &7is a test", '&')));
		assertEquals("This x7is xra test", replaceColorCodeWithx( 
												translateColorCodes("This &7is &Ra test", '&')));

		// Test without quotes:
		assertEquals("This x7is xra x1tx2ex3sx4t", replaceColorCodeWithx( 
												translateColorCodes("This &7is &Ra &1t&2e&3s&4t", '&')));

		
		// Test with quotes:
		assertEquals("This x7is xra &1t&2e&3s&4t", replaceColorCodeWithx( 
												translateColorCodes("This &7is &Ra \\Q&1t&2e&3s&4t\\E", '&')));
		
		// Test with 2 quotes:
		String results1 = replaceColorCodeWithx( 
				translateColorCodes("This &7is &Ra \\Q&1t&2e&3s&4t\\E &7and \\Q&1m&2o&3r&4e\\E", '&') );
		assertEquals("This x7is xra &1t&2e&3s&4t x7and &1m&2o&3r&4e", results1 );
		
	}
	
	/**
	 * <p>Running the junit test above in the IDE works well using the actual character that is
	 * referred to with the char code 167.  But running through gradle it has an issue with translation
	 * on the character code set and is not always the same character. </p>
	 * 
	 * <p>So to provide consistency and eliminate the UTF junk, we are converting it to a lower
	 * case x so it can at least test the function and not the character encoding of gradle.
	 * <p>
	 * 
	 * @param text
	 * @return
	 */
	private String replaceColorCodeWithx( String text ) {
		
		return replaceColorCodeWithx( text, 'x' );
		
	}
	private String replaceColorCodeWithx( String text, char replace ) {
		
		char code = 167;
		
		return text.replace( code, replace );
		
	}
	
	@Test
	public void testHexColors() {
		
		// Test no translations:
		assertEquals("This is a test", translateHexColorCodes("This is a test", Text.COLOR_CHAR));
		
		// test simple translations:
		assertEquals("This &7is a test", translateHexColorCodes("This &7is a test", Text.COLOR_CHAR));
		
		// Test a hex color code:
		assertEquals("This &7is ^x^a^3^b^4^c^5 &Ra test", replaceColorCodeWithx(
										translateHexColorCodes("This &7is #a3b4c5 &Ra test", Text.COLOR_CHAR), '^' ));

//		// Test without quotes:
//		assertEquals("This x7is xra x1tx2ex3sx4t", replaceColorCodeWithx( 
//												translateColorCodes("This &7is &Ra &1t&2e&3s&4t", '&')));

		// Test the translation within main:
		assertEquals("This ^7is ^x^a^3^b^4^c^5 ^ra test", replaceColorCodeWithx(
										translateColorCodes("This &7is #a3b4c5 &Ra test", '&'), '^' ));

		// Test with complete quote:
		assertEquals("This ^7is ^x^a^3^b^4^c^5 ^ra test #123456 test", replaceColorCodeWithx(
				translateColorCodes("This &7is #a3b4c5 &Ra test \\Q#123456 test\\E", '&'), '^' ));
		
		
		// Test with the alt hex encoding that could be used with placeholder hex support:
		assertEquals("This ^7is ^x^a^3^b^4^c^5 ^ra test ^x^1^2^3^4^5^6 test", replaceColorCodeWithx(
				translateAmpColorCodesAltHexCode("This &7is #a3b4c5 &Ra test #123456 test"), '^' ));
		
		// Test with two complete quote:
		assertEquals("This ^7is ^x^a^3^b^4^c^5 ^ra test #123456 test test2 #778899 test", replaceColorCodeWithx(
				translateColorCodes("This &7is #a3b4c5 &Ra test \\Q#123456 test\\E test2 \\Q#778899 test\\E", '&'), '^' ));
	
	}
	
	@Test
	public void testHexColorBug() {
		
		// Note: These is a problem with hex color codes being used in lore (or elsewhere probably)
		//       where they are not being translated unless there is another color, such as &7
		//       Proceeding it.  So try to reproduce the situation...
		
		// The problem was that the hex conversion was working perfectly well.  But 
		// since the "dirty" variable was not getting modified, since the hex colors were
		// applied before that point in processing, it would always revert back to the original 
		// unchanged String value because it thought there was nothing that changed.
		
		// To fix the problem, I eliminated the dirty variable and am always converting the byte
		// array back to a String value.  So no need to check if dirty anymore, since it always converts.
		assertEquals("This is ^x^a^3^b^4^c^5 a test", replaceColorCodeWithx(
				translateAmpColorCodes("This is #a3b4c5 a test" ), '^' ));

		assertEquals("This is ^x^a^3^b^4^c^5a test", replaceColorCodeWithx(
				translateAmpColorCodes("This is &#a3b4c5a test" ), '^' ));
		
		
	}
	
	
	@Test
	public void testBadRegexBlockQuote() {
		
		
		// Test with quotes just to show it works:
		String results1 = replaceColorCodeWithx( 
				translateColorCodes("This &7is \\Q&1t&2e\\E", '&'));
		assertEquals("This x7is &1t&2e", results1 );
		assertEquals( 16, results1.length() );
		
		// Test with no ending quotes:
		String results2= replaceColorCodeWithx( 
				translateColorCodes("A&7b \\Q&1c&2d", '&'));
		assertEquals("Ax7b &1c&2d", results2);
		assertEquals( 11, results2.length() );
		

	}
}
