package Main.CloudToMongo;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.mongodb.*;
import com.mongodb.util.JSON;

import java.util.*;
import java.io.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CloudToMongo  implements MqttCallback {
    MqttClient mqttclient;
    static MongoClient mongoClient;
    static DB db;
    static DBCollection mongocol;
    static String mongo_user = new String();
    static String mongo_password = new String();
    static String mongo_address = new String();
    static String cloud_server = new String();
    static String cloud_topic = new String();
    static String mongo_host = new String();
    static String mongo_replica = new String();
    static String mongo_database = new String();
    static String mongo_collection = new String();
    static String mongo_authentication = new String();
    static JTextArea documentLabel = new JTextArea("\n");


    private static void createWindow() {
        JFrame frame = new JFrame("Cloud to Mongo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLabel textLabel = new JLabel("Data from broker: ",SwingConstants.CENTER);
        textLabel.setPreferredSize(new Dimension(600, 30));
        JScrollPane scroll = new JScrollPane (documentLabel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setPreferredSize(new Dimension(600, 200));
        JButton b1 = new JButton("Stop the program");
        frame.getContentPane().add(textLabel, BorderLayout.PAGE_START);
        frame.getContentPane().add(scroll, BorderLayout.CENTER);
        frame.getContentPane().add(b1, BorderLayout.PAGE_END);
        frame.setLocationRelativeTo(null);
        frame.pack();
        frame.setVisible(true);
        b1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                System.exit(0);
            }
        });
    }

    public static void textGui(){
        System.out.println("------------- Cloud to Mongo App -------------");
        System.out.println("1. Inserir dados de movimento");
        System.out.println("2. Inserir dados de temperatura");
        System.out.print("Opção: ");
    }

    public static void userChoice(){
        Scanner s = new Scanner(System.in);
        switch (s.nextInt()){
            case 1:
                cloud_topic = "pisid_mazemov";
                break;
            case 2:
                cloud_topic = "pisid_mazetemp";
                break;
        }
    }

    public static void main(String[] args) {
        textGui();
        userChoice();
        createWindow();
        try {
            Properties p = new Properties();
            p.load(new FileInputStream("src/main/java/Main/CloudToMongo/CloudToMongo.ini"));
            mongo_address = p.getProperty("mongo_address");
            mongo_user = p.getProperty("mongo_user");
            mongo_password = p.getProperty("mongo_password");
            mongo_replica = p.getProperty("mongo_replica");
            cloud_server = p.getProperty("cloud_server");
            mongo_host = p.getProperty("mongo_host");
            mongo_database = p.getProperty("mongo_database");
            mongo_authentication = p.getProperty("mongo_authentication");
            mongo_collection = p.getProperty("mongo_collection");
        } catch (Exception e) {
            System.out.println("Error reading CloudToMongo.ini file " + e);
            JOptionPane.showMessageDialog(null, "The CloudToMongo.inifile wasn't found.", "Main/CloudToMongo", JOptionPane.ERROR_MESSAGE);
        }
        new CloudToMongo().connecCloud();
        new CloudToMongo().connectMongo();
    }

    public void connecCloud() {
        int i;
        try {
            i = new Random().nextInt(100000);
            mqttclient = new MqttClient(cloud_server, "CloudToMongo_"+String.valueOf(i)+"_"+cloud_topic);
            mqttclient.connect();
            mqttclient.setCallback(this);
            mqttclient.subscribe(cloud_topic);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void connectMongo() {
        String mongoURI = new String();
        mongoURI = "mongodb+srv://java:java@atlascluster.1qn0igi.mongodb.net/?retryWrites=true&w=majority";
        MongoClient mongoClient = new MongoClient(new MongoClientURI(mongoURI));
        db = mongoClient.getDB(mongo_database);
        mongocol = db.getCollection(mongo_collection);
    }

    @Override
    public void messageArrived(String topic, MqttMessage c)
            throws Exception {
        try {
            DBObject document_json;
            document_json = (DBObject) JSON.parse(c.toString());
            mongocol.insert(document_json);
            documentLabel.append(c.toString()+"\n");
        } catch (Exception e) {

        }
    }

    @Override
    public void connectionLost(Throwable cause) {
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }
}