package icons;

import javax.swing.Icon;

import com.intellij.openapi.util.IconLoader;

/**
 * The Icon interface that is used by the plugin xml file
 *
 * @author Tavan Alain
 */

public interface SwissAsIcons {
	Icon CRITICAL = IconLoader.getIcon("/icons/critical.png");
	Icon BUBBLE = IconLoader.getIcon("/icons/bubble.png");
	Icon DELETE = IconLoader.getIcon("/icons/delete.png");
	Icon SONAR = IconLoader.getIcon("/icons/sonar.png");
	Icon WARNING = IconLoader.getIcon("/icons/warning.png");
	Icon AMOS = IconLoader.getIcon("/icons/amos.png");
	Icon DTO = IconLoader.getIcon("/icons/dto.png");
	Icon GENERICDTO = IconLoader.getIcon("/icons/genericdto.png");
	Icon WHOIS = IconLoader.getIcon("/icons/whois.png");
	Icon AUTHOR = IconLoader.getIcon("/icons/author.png");
	Icon SUPPORT = IconLoader.getIcon("/icons/support.png");
	Icon REVIEW = IconLoader.getIcon("/icons/review.png");
	Icon LOOK_FOR_CAUSE = IconLoader.getIcon("/icons/magn.png");
	Icon ISSUE_FIXED = IconLoader.getIcon("/icons/activate.png");
	Icon ISSUE = IconLoader.getIcon("/icons/lightbulb_on.png");
}
