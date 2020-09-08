package com.swissas.toolwindow;

import com.swissas.beans.*;
import com.swissas.util.SwissAsStorage;
import info.debatty.java.stringsimilarity.JaroWinkler;
import org.jetbrains.annotations.NotNull;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Helper class for Warning Content
 *
 * @author Tavan Alain
 */
public final class WarningContentHelper {

    private static final JaroWinkler JARO_WINKLER = new JaroWinkler();
    
    private WarningContentHelper() {
        throw new IllegalStateException("Utility class");
    }
    public static void generateTypeFromElementTypeAndAddItToTypeSet(Element elementType,@NotNull Set<Type> types, @NotNull Map<String, Directory> directories) {
        String typeName = elementType.attr("name");
        Type type = new Type(elementType);
        elementType.childNodes().stream().flatMap(e -> e.childNodes().stream()).forEach(fileNode -> {
            String fullPath = fileNode.attr("path");
            fullPath = fullPath.substring(0, fullPath.lastIndexOf("/"));
            type.addChildren(generateDirectory(directories, fullPath, "", fileNode, typeName));
        });
        types.add(type);
    }

    private static Directory generateDirectory(Map<String, Directory> directories, String folderName, String parentPath, Node fileNode, String typeName) {
        int indexOf = folderName.indexOf("/");
        Directory dir;
        if(indexOf > -1) {
            String topFolder = folderName.substring(0, indexOf);
            dir = directories.computeIfAbsent(typeName + "-> " + parentPath + topFolder,  k-> new Directory(topFolder, parentPath));
            dir.addChildren(generateDirectory(directories, folderName.substring(indexOf+1), parentPath + topFolder + "/", fileNode, typeName));
        }else {
            dir = directories.computeIfAbsent(typeName + "-> " + parentPath + folderName,  k-> new Directory(folderName, parentPath));
            String path = fileNode.attr("path");
            String fileName = path.substring(path.lastIndexOf("/")+1);
            dir.addChildren(new File(fileName, fileNode));
        }
        return dir;
    }

    public static void fillTreeWithChildren(WarningContentTreeNode node, AttributeChildrenBean element, String filterLetterCode, boolean onlyCritical, String similarMessage) {
        WarningContentTreeNode currentElement = new WarningContentTreeNode("");
        if(element instanceof File) {
            currentElement.setCurrentType(WarningContentTreeNode.TreeType.File);
            if(fileNotGoodResponsible((File) element, filterLetterCode)){
                return;
            }
            currentElement.setMine(((File)element).isMine());
        }else if(element instanceof Message) {
            Message message = (Message)element; 
            currentElement.setCurrentType(WarningContentTreeNode.TreeType.Message);
            double score = similarMessage == null ? 1.0 : JARO_WINKLER.similarity(message.getDescription(), similarMessage);
            if(onlyCritical && !message.isCritical() || score < SwissAsStorage.getInstance().getSimilarValue()) {
                return;
            }
        }

        Set<AttributeChildrenBean> children = element.getChildren();

        if(element instanceof Directory) {
            StringBuilder sb = new StringBuilder();
            Directory dir = getSingleDirectoryPath(sb, (Directory) element, filterLetterCode);
            currentElement.setUserObject(sb.toString());
            children = dir.getChildren();
        }
        children.forEach(child -> fillTreeWithChildren(currentElement, child, filterLetterCode, onlyCritical, similarMessage));


        if(currentElement.isLeaf()) {
            if(element instanceof Message) {
                currentElement.setUserObject(element.getText());
            }else {
                //not a message, can't be a valid leaf, therefore ignore
                return;
            }

        }else {
            String text = currentElement.getUserObject().toString().isBlank() ? element.getText()
                                                                              : currentElement.getUserObject().toString();
            currentElement.setUserObject(text);

        }
        node.add(currentElement);
    }

    private static boolean fileNotGoodResponsible(File element, String filterLetterCode) {
        return filterLetterCode != null &&
                !filterLetterCode.equals(SwissAsStorage.getInstance().getMyTeam()) &&
                !filterLetterCode.equals(element.getResponsible());
    }


    private static Directory getSingleDirectoryPath(StringBuilder sb, Directory directory, String filteredResponsible) {
        Directory dir;
        sb.append(directory.getMainAttribute());
        List<Directory> subDirs = directory.getChildren().stream()
                .filter(Directory.class::isInstance)
                .map(Directory.class::cast)
                .filter(subDir -> hasFilteredChild(subDir, filteredResponsible)).collect(Collectors.toList());
        if(subDirs.size() == 1) {
            Directory childDirectory = subDirs.get(0);
            sb.append("/");
            dir =  getSingleDirectoryPath(sb, childDirectory, filteredResponsible);
        }else {
            dir = directory;
        }
        return dir;
    }
    
    private static boolean hasFilteredChild(Directory directory, String filteredResponsible) {
        List<Directory> subDirs = directory.getChildren().stream()
                .filter(Directory.class::isInstance)
                .map(Directory.class::cast)
                .collect(Collectors.toList());
        Stream<File> files = directory.getChildren().stream()
                .filter(File.class::isInstance)
                .map(File.class::cast);
        if(filteredResponsible != null && !filteredResponsible.equals(SwissAsStorage.getInstance().getMyTeam())) {
            files = files.filter(file -> file.getResponsible().equals(filteredResponsible));
        }
        if(files.count() > 0L) {
            return true;
        }
        for (Directory subDir : subDirs) {
            if(hasFilteredChild(subDir, filteredResponsible)){
                return true;
            }
        }
        return false;
    }
}