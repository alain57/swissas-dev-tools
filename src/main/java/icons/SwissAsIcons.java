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
}
