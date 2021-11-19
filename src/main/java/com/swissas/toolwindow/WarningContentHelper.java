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
        var typeName = elementType.attr("name");
        var type = new Type(elementType);
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
            var topFolder = folderName.substring(0, indexOf);
            dir = directories.computeIfAbsent(typeName + "-> " + parentPath + topFolder,  k-> new Directory(topFolder, parentPath));
            dir.addChildren(generateDirectory(directories, folderName.substring(indexOf+1), parentPath + topFolder + "/", fileNode, typeName));
        }else {
            dir = directories.computeIfAbsent(typeName + "-> " + parentPath + folderName,  k-> new Directory(folderName, parentPath));
            var path = fileNode.attr("path");
            var fileName = path.substring(path.lastIndexOf("/")+1);
            dir.addChildren(new File(fileName, fileNode));
        }
        return dir;
    }

    private static boolean hasSimilarMessage(Message message, String similarMessage) {
        double score = similarMessage == null ? Double.MAX_VALUE : JARO_WINKLER.similarity(message.getDescription(), similarMessage);
        return score > SwissAsStorage.getInstance().getSimilarValue();
    }
    
    public static void fillTreeWithChildren(WarningContentTreeNode node, AttributeChildrenBean element, 
                                            String filterLetterCode, boolean onlyCritical, String similarMessage) {
        var currentElement = new WarningContentTreeNode("");
        if(element instanceof File) {
            currentElement.setCurrentType(WarningContentTreeNode.TreeType.FILE);
            if(fileNotGoodResponsible((File) element, filterLetterCode)){
                return;
            }
            currentElement.setMine(((File)element).isMine());
        }else if(element instanceof Message) {
            var message = (Message)element; 
            currentElement.setCurrentType(WarningContentTreeNode.TreeType.MESSAGE);
            if(onlyCritical && !message.isCritical() || !hasSimilarMessage(message, similarMessage)) {
                return;
            }
        }

        Set<AttributeChildrenBean> children = element.getChildren();

        if(element instanceof Directory) {
            var sb = new StringBuilder();
            var dir = getSingleDirectoryPath(sb, (Directory) element, filterLetterCode, onlyCritical, similarMessage);
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


    private static Directory getSingleDirectoryPath(StringBuilder sb, Directory directory, String filteredResponsible, boolean onlyCritical, String similarMessage) {
        Directory dir;
        sb.append(directory.getMainAttribute());
        Set<AttributeChildrenBean> children = directory.getChildren();
        boolean dirContainsFiles = children.stream()
                                       .filter(File.class::isInstance)
                                       .map(File.class::cast)
                                       .filter(f -> !fileNotGoodResponsible(f, filteredResponsible))
                                       .flatMap(f -> f.getChildren().stream())
                                       .filter(Message.class::isInstance)
                                       .map(Message.class::cast)
                                       .anyMatch(message -> hasSimilarMessage(message, similarMessage));
                                    
        if(dirContainsFiles){
            return directory;
        }
        List<Directory> subDirs = children.stream()
                                          .filter(Directory.class::isInstance)
                                          .map(Directory.class::cast)
                                          .filter(subDir -> hasFilteredChild(subDir, filteredResponsible, onlyCritical, similarMessage)).collect(Collectors.toList());
        if(subDirs.size() == 1) {
            var childDirectory = subDirs.get(0);
            sb.append("/");
            dir =  getSingleDirectoryPath(sb, childDirectory, filteredResponsible, onlyCritical, similarMessage);
        }else {
            dir = directory;
        }
        return dir;
    }
    
    private static boolean hasFilteredChild(Directory directory, String filteredResponsible, boolean onlyCritical, String similarMessage) {
        List<Directory> subDirs = directory.getChildren().stream()
                .filter(Directory.class::isInstance)
                .map(Directory.class::cast)
                .collect(Collectors.toList());
        Stream<File> files = directory.getChildren().stream()
                .filter(File.class::isInstance)
                .map(File.class::cast);
        if(filteredResponsible != null){
            files = files.filter(file -> file.getResponsible().equals(filteredResponsible));
        }
        if(onlyCritical) {
            files = files.filter(file -> file.getChildren().stream()
                                             .filter(Message.class::isInstance)
                                             .map(Message.class::cast)
                                             .anyMatch(message -> message.isCritical()));
        }
        if(similarMessage != null) {
            files = files.filter(file -> file.getChildren().stream()
                                  .filter(Message.class::isInstance)
                                  .map(Message.class::cast)
                                  .anyMatch(message -> hasSimilarMessage(message, similarMessage)));
        }
        if(files.count() > 0L) {
            return true;
        }
        for (Directory subDir : subDirs) {
            if(hasFilteredChild(subDir, filteredResponsible, onlyCritical, similarMessage)){
                return true;
            }
        }
        return false;
    }
}