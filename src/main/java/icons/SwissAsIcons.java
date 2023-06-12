package icons;

import javax.swing.Icon;

import com.intellij.openapi.util.IconLoader;

/**
 * The Icon interface that is used by the plugin xml file
 *
 * @author Tavan Alain
 */

public interface SwissAsIcons {
	Icon CRITICAL = getIcon("/icons/critical.png");
	Icon BUBBLE = getIcon("/icons/bubble.png");
	Icon DELETE = getIcon("/icons/delete.png");
	Icon SONAR = getIcon("/icons/sonar.png");
	Icon WARNING = getIcon("/icons/warning.png");
	Icon AMOS = getIcon("/icons/amos.png");
	Icon DTO = getIcon("/icons/dto.png");
	Icon GENERICDTO = getIcon("/icons/genericdto.png");
	Icon WHOIS = getIcon("/icons/whois.png");
	Icon AUTHOR = getIcon("/icons/author.png");
	Icon SUPPORT = getIcon("/icons/support.png");
	Icon REVIEW = getIcon("/icons/review.png");
	
	private static Icon getIcon(String path) {
		return IconLoader.getIcon(path, SwissAsIcons.class);
	}
}
