package Server;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;

import Shared.Tuple;

public class UserManager {
    ConcurrentHashMap<String, Tuple> usersDatabase;
    FileWriter output;
    Boolean empty_output;

    public UserManager(ConcurrentHashMap<String, Tuple> usersDatabase) {
        this.usersDatabase = usersDatabase;
        this.empty_output = true;

        try {
            File users = new File("users.json");
            if (!users.exists()) {
                users.createNewFile();
                output = new FileWriter("users.json", true);
                output.write("{\n\"users\": [");
                output.flush();
            } else {
                output = new FileWriter("users.json", true);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    synchronized public void send(User u) {

        //persisting a user forcing json syntax

        Gson gson = new Gson();

        try (FileWriter output = new FileWriter("users.json", true)) {
            if (empty_output) {
                output.write("\n" + gson.toJson(u));
                empty_output = false;
            } else
                output.write("\n," + gson.toJson(u));
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getUsers() {

        // recovering users from the file and putting them in the database
        // forcing json syntax

        BufferedReader reader;
        int n = 0;
        Gson gson = new Gson();

        try {
            reader = new BufferedReader(new FileReader("users.json"));
            String line = reader.readLine();
            line = reader.readLine();
            line = reader.readLine();

            while (line != null) {
                if (line.equals("]}")) {
                    break;
                }

                User u;
                
                if(n==0){
                u = gson.fromJson(line, User.class);
                } else {
                u = gson.fromJson(line.substring(1), User.class);
                }

                usersDatabase.put(u.getUser(), new Tuple(null, false, null, 0));
                usersDatabase.get(u.getUser()).setPassword(u.getPassword());
                usersDatabase.get(u.getUser()).setStatus(false);

                line = reader.readLine();
                n++;
            }

            reader.close();

            output = new FileWriter("users.json", false);
            output.write("{\n\"users\": [");
            output.flush();
            this.empty_output = true;

            
            for (String key : usersDatabase.keySet()) {
                User user = new User(key, usersDatabase.get(key).getPassword());
                send(user);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("User database recovered: " + n + " users.");
    }

    public void close() {
        try {
            output = new FileWriter("users.json", true);
            output.write("\n]}");
            output.flush();
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}