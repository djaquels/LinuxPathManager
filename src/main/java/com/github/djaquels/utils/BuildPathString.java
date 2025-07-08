package com.github.djaquels.utils;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public  final   class BuildPathString {
    private static BuildPathString  client;
    private BuildPathString(){}

    public static BuildPathString getBuildPathClient(){
        if(client == null){
            client = new BuildPathString();
        }
        return client;
    }

	public static List<String> filterIncompleteSubstrings(List<String> strings, List<String> filters) {
        List<String> result = new ArrayList<>();

        for (String str : strings) {
            boolean isIncompleteSubstring = false;

            // Vérifie si la chaîne est une sous-chaîne incomplète de l'une des chaînes de filtrage
            for (String filter : filters) {
                if (filter.contains(str) && !filter.equals(str) && !filters.contains(str)) {
                    isIncompleteSubstring = true;
                    break;
                }
            }

            // Si ce n'est pas une sous-chaîne incomplète, ajoute à la liste résultat
            if (!isIncompleteSubstring) {
                result.add(str);
            }
        }

        return result;
    }

    public  String buildPathString(String currentPath, String newPath, String shell) {
        // tuples of PATH for common shells bash i.e
	// command, path separator, line separator
	// bash => (export PATH=, =, :)
	// csh => (setenv PATH, "setenv PATH ", :)
	// fish => (set -gx PATH, "set -gx PATH ", " ")
	HashMap<String, String[]> shellTuples = new HashMap<>();
	shellTuples.put("bash", new String[]{"export PATH=", "=", ":"});
	shellTuples.put("zsh", new String[]{"export PATH=", "=", ":"});
	shellTuples.put("csh", new String[]{"setenv PATH ", "setenv PATH ", ":"});
	shellTuples.put("tcsh", new String[]{"setenv PATH ", "setenv PATH ", ":"});
	shellTuples.put("fish", new String[]{"set -gx PATH ", "set -gx PATH ", " "});
	// Build the new path string
	String result = shellTuples.get(shell)[0];
        String[] currentPathString = currentPath.split(shellTuples.get(shell)[1]);
	List<String> currentPathList = Arrays.asList(currentPathString[currentPathString.length-1].split(shellTuples.get(shell)[2]));
        Set<String> newPathList = new HashSet<>(Arrays.asList(newPath.split(":"))); //this is the standard from memory
        //clean strings removing \n and \r and trimming
	Set<String> cleanNewPathList = new HashSet<>();
	for(String current: newPathList){
          String clean = current.replaceAll("[\\n\\r]", "").trim();
	  if(!clean.isEmpty()){
	    cleanNewPathList.add(clean);
	  }
        }
	cleanNewPathList.add("/bin");
	cleanNewPathList.add("/usr/bin");//always add these two paths for security reasons
    result = result + String.join(shellTuples.get(shell)[2], cleanNewPathList);
    return result;
    }

}
