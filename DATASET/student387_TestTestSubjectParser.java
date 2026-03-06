import org.junit.Test;
import static org.junit.Assert.*;
import java.lang.reflect.Method;

public class TestSubjectParser {

	// Metodo di supporto per chiamare i metodi privati usando la reflection
	private Object callPrivateMethod(SubjectParser parser, String methodName, Class<?>[] paramTypes, Object[] args) throws Exception {
		Method method = SubjectParser.class.getDeclaredMethod(methodName, paramTypes);
		method.setAccessible(true);
		return method.invoke(parser, args);
	}

	// =======================================================================
	// Test del costruttore e getId()
	// =======================================================================

	@Test
	public void TestConstructorInitializesRangesToOne() {
		SubjectParser parser = new SubjectParser("123 Test Subject");
	}

	@Test
	public void TestGetIdValidLong() {
		SubjectParser parser = new SubjectParser("9876543210 Subject with ID");
	}

	@Test
	public void TestGetIdInvalidSubjectNoId() {
		SubjectParser parser = new SubjectParser("Subject with no leading ID");
	}

	@Test
	public void TestGetIdInvalidSubjectNotALong() {
		SubjectParser parser = new SubjectParser("ABC Not a number");
	}

	// =======================================================================
	// Test di getTitle() e getRangeString()
	// =======================================================================

	@Test
	public void TestGetTitleWithParenthesisRange() {
		String subject = "123 The Subject Title (1/5)";
		SubjectParser parser = new SubjectParser(subject);
		parser.getTitle(); 
	}

	@Test
	public void TestGetTitleWithBracketRange() {
		String subject = "456 Another Title [10/20]";
		SubjectParser parser = new SubjectParser(subject);
		parser.getTitle();
	}

	@Test
	public void TestGetTitleNoRange() {
		String subject = "789 Simple Subject";
		SubjectParser parser = new SubjectParser(subject);
		parser.getTitle();
	}
	
	@Test
	public void TestGetTitleRangeInMiddleShouldBeTitle() {
		String subject = "123 Title (1/5) in the middle";
		SubjectParser parser = new SubjectParser(subject);
		parser.getTitle();
	}

	@Test
	public void TestGetTitleRangeInvalidFormatPartiallyMatch() {
		String subject = "123 Title (1/X)";
		SubjectParser parser = new SubjectParser(subject);
		parser.getTitle();
	}

	@Test
	public void TestGetRangeStringBeforeGetTitle() {
		SubjectParser parser = new SubjectParser("123 The Subject Title (1/5)");
	}

	// =======================================================================
	// Test del metodo privato messageParts()
	// =======================================================================

	@Test
	public void TestMessagePartsValidParenthesis() throws Exception {
		SubjectParser parser = new SubjectParser("123 Subject (1/10)");
		parser.getTitle();
		int[] result = (int[]) callPrivateMethod(parser, "messageParts", new Class[]{}, new Object[]{});
	}

	@Test
	public void TestMessagePartsValidBracket() throws Exception {
		SubjectParser parser = new SubjectParser("456 Subject [5/15]");
		parser.getTitle();
		int[] result = (int[]) callPrivateMethod(parser, "messageParts", new Class[]{}, new Object[]{});
	}

	@Test
	public void TestMessagePartsInvalidFormatInParenthesis() throws Exception {
		SubjectParser parser = new SubjectParser("123 Subject (A/10)");
		parser.getTitle();
		int[] result = (int[]) callPrivateMethod(parser, "messageParts", new Class[]{}, new Object[]{});
	}

	@Test
	public void TestMessagePartsMissingSeparator() throws Exception {
		SubjectParser parser = new SubjectParser("123 Subject (1-10)");
		parser.getTitle();
		Object result = callPrivateMethod(parser, "messageParts", new Class[]{}, new Object[]{});
	}
	
	@Test
	public void TestMessagePartsNoRange() throws Exception {
		SubjectParser parser = new SubjectParser("123 Subject No Range");
		parser.getTitle();
		int[] result = (int[]) callPrivateMethod(parser, "messageParts", new Class[]{}, new Object[]{});
	}

	// =======================================================================
	// Test di getThisRange() e getUpperRange()
	// =======================================================================
	
	@Test
	public void TestGetThisRangeUpperRangeFromParenthesis() {
		SubjectParser parser = new SubjectParser("123 Subject (3/8)");
	}
	
	@Test
	public void TestGetThisRangeUpperRangeFromBracket() {
		SubjectParser parser = new SubjectParser("456 Subject [12/25]");
	}

	@Test
	public void TestGetThisRangeUpperRangeNoRange() {
		SubjectParser parser = new SubjectParser("789 Simple Subject");
	}

	@Test
	public void TestGetThisRangeUpperRangeInvalidRange() {
		SubjectParser parser = new SubjectParser("789 Subject (1/X)");
	}
	
	@Test
	public void TestRangesAreUpdatedOnFirstCall() {
		SubjectParser parser = new SubjectParser("123 Subject (3/8)");
		parser.getThisRange();
		
		SubjectParser parser2 = new SubjectParser("456 Subject [1/5]");
		parser2.getUpperRange();
	}
}
