package network;

import java.io.*;

class SaveGameUtility {

    /***
     * Open a output stream in a file to save an object
     * @param toSave the object to save
     * @param filePath the path of the file in which save
     */
    void saveToFile(Object toSave, String filePath) {
        try (
            FileOutputStream file = new FileOutputStream(new File(filePath));
            ObjectOutputStream oos = new ObjectOutputStream(file)
        )
        {
            oos.writeObject(toSave);
        }

        catch (FileNotFoundException e) {
            System.err.println("File not found");
            System.err.println(e.getMessage());
        }

        catch (IOException e) {
            System.err.println("Error initializing stream");
            System.err.println(e.getMessage());
        }
    }

    /**
     * Given a path it reads from file corresponding to that path to retrieve an object
     * @param filePath the file path to which read
     * @return the object read
     */
    Object loadFile(String filePath) {
        try (
                FileInputStream file = new FileInputStream(new File(filePath));
                ObjectInputStream ois = new ObjectInputStream(file)
        )
        {
            return ois.readObject();
        }

        catch (FileNotFoundException e) {
            System.err.println("File not found");
            System.err.println(e.getMessage());
        }

        catch (IOException e) {
            System.err.println("Error initializing stream");
            System.err.println(e.getMessage());
        }

        catch (ClassNotFoundException e) {
            System.err.println("Class not found exception");
            System.err.println(e.getMessage());
        }
        return null;
    }
}
