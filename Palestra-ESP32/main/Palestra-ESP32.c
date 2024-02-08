/*******************************************
Author: Michael Giardina
Purpose: This program will:
            1) Read in data from an MPU6050 
            2) Convert the raw data to useful
                data points
            3) Send that data to a server 
*******************************************/
//includes
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <stdbool.h>
#include <math.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <netinet/in.h>
#include <netdb.h>
#include <errno.h>
#include "esp_log.h"
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "freertos/event_groups.h"
#include "esp_system.h"
#include "esp_wifi.h"
#include "esp_event.h"
#include "nvs_flash.h"
#include "driver/i2c.h"
#include "sdkconfig.h"
#include "lwip/sockets.h"
#include "lwip/sys.h"
#include "lwip/dns.h"
#include "lwip/netdb.h"
#include "lwip/err.h"
#include <limits.h>
//constants for the MPU6050
#define SDA 21
#define SCL 22
#define CLK_SPEED 1000000
#define ACK_EN 1
#define ACK_DN 0

//constants for the wifi status
#define WIFI_SUCCESS 1 << 0
#define WIFI_FAILURE 1 << 1
#define TCP_SUCCESS 1 << 0
#define TCP_FAILURE 1 << 1
#define SSID "GIARDIM"
#define PASSWORD "superSecretPassword"

//constanst for the wifi group
static EventGroupHandle_t wifiEventLoop;
static uint8_t cntTries = 0;

//adresses of the MPU6050
#define I2C_ADDR 0x68
#define POWER_MGT 0x6B
#define MPU6050_ACCEL_XOUT_H 0x3B
#define MPU6050_ACCEL_XOUT_L 0x3C
#define MPU6050_ACCEL_YOUT_H 0x3D
#define MPU6050_ACCEL_YOUT_L 0x3E
#define MPU6050_ACCEL_ZOUT_H 0x3F
#define MPU6050_ACCEL_ZOUT_L 0x40
#define MPU6050_TEMP_OUT_H 0x41
#define MPU6050_TEMP_OUT_L 0x42
#define MPU6050_GYRO_XOUT_H 0x43
#define MPU6050_GYRO_XOUT_L 0x44
#define MPU6050_GYRO_YOUT_H 0x45
#define MPU6050_GYRO_YOUT_L 0x46
#define MPU6050_GYRO_ZOUT_H 0x47
#define MPU6050_GYRO_ZOUT_L 0X48

//struct for the mpu6050 input data
struct data_t{
    float accel_x;
    float accel_y;
    float accel_z;
}typedef data_t;

//Reading values from the MPU6050 via I2C
void i2cMasterInit(data_t *data){
    //variables
    const char *TAG = "i2cMasterInit";
	static bool isInstalled = false;
	//accocated 14 addresses for the 14 addresses in the mpu6050, not sure if we will need them all
    uint8_t buffer[14] = {0}; 
    uint8_t pitch = 0;
    uint8_t roll = 0;
    uint8_t yaw = 0;

    //configure the master
    i2c_config_t masterCfg = {
        .mode = I2C_MODE_MASTER,
        .sda_io_num = SDA,
        .sda_pullup_en = GPIO_PULLUP_ENABLE,
        .scl_io_num = SCL,
        .scl_pullup_en = GPIO_PULLUP_ENABLE,
        .master.clk_speed = CLK_SPEED,
    };

    //set the configuration
    ESP_ERROR_CHECK(i2c_param_config(I2C_NUM_0, &masterCfg));
    
	if (!isInstalled){
    	//install the driver
    	ESP_ERROR_CHECK(i2c_driver_install(I2C_NUM_0, I2C_MODE_MASTER, 0, 0, 0));
    	isInstalled = true;
	}
    //initialize the i2c master 
    i2c_cmd_handle_t masterCMD = i2c_cmd_link_create();
    
    //start the communication
    ESP_ERROR_CHECK(i2c_master_start(masterCMD));

    //set the master to write
    ESP_ERROR_CHECK(i2c_master_write_byte(masterCMD, (I2C_ADDR << 1) | I2C_MASTER_WRITE, ACK_EN));

    //set the powerMgt bit 
    ESP_ERROR_CHECK(i2c_master_write_byte(masterCMD, POWER_MGT, ACK_EN));
    
    //set start bit
    ESP_ERROR_CHECK(i2c_master_write_byte(masterCMD, 0, ACK_EN));

    //stop the master
    ESP_ERROR_CHECK(i2c_master_stop(masterCMD));

    //start sending queued commands
    ESP_ERROR_CHECK(i2c_master_cmd_begin(I2C_NUM_0, masterCMD, (1000 / portTICK_PERIOD_MS)));

    //delete the command link
    i2c_cmd_link_delete(masterCMD);
    
        //position the internal register pointer to the x output
        masterCMD = i2c_cmd_link_create();
        ESP_ERROR_CHECK(i2c_master_start(masterCMD));       
        ESP_ERROR_CHECK(i2c_master_write_byte(masterCMD, (I2C_ADDR << 1) | I2C_MASTER_WRITE, ACK_EN));
        ESP_ERROR_CHECK(i2c_master_write_byte(masterCMD, MPU6050_ACCEL_XOUT_H, ACK_EN));
        ESP_ERROR_CHECK(i2c_master_stop(masterCMD));
        ESP_ERROR_CHECK(i2c_master_cmd_begin(I2C_NUM_0, masterCMD, (1000/portTICK_PERIOD_MS)));    
        i2c_cmd_link_delete(masterCMD);

        //start reading the data
        masterCMD = i2c_cmd_link_create();
        ESP_ERROR_CHECK(i2c_master_start(masterCMD)); 
        ESP_ERROR_CHECK(i2c_master_write_byte(masterCMD, (I2C_ADDR << 1) | I2C_MASTER_READ, ACK_EN));
        
        //starting at the x accel_h it reads down to z accel_l (all accel values)
        ESP_ERROR_CHECK(i2c_master_read_byte(masterCMD, buffer, ACK_DN)); 
        ESP_ERROR_CHECK(i2c_master_read_byte(masterCMD, buffer + 1, ACK_DN));
        ESP_ERROR_CHECK(i2c_master_read_byte(masterCMD, buffer + 2, ACK_DN));
        ESP_ERROR_CHECK(i2c_master_read_byte(masterCMD, buffer + 3, ACK_DN));
        ESP_ERROR_CHECK(i2c_master_read_byte(masterCMD, buffer + 4, ACK_DN));
        ESP_ERROR_CHECK(i2c_master_read_byte(masterCMD, buffer + 5, ACK_EN));

        //stop reading the data
        ESP_ERROR_CHECK(i2c_master_stop(masterCMD));
        ESP_ERROR_CHECK(i2c_master_cmd_begin(I2C_NUM_0, masterCMD, (1000/portTICK_PERIOD_MS)));
        i2c_cmd_link_delete(masterCMD);

        //The calculations to convert the raw data to g's comes from;
        //http://ozzmaker.com/accelerometer-to-g/
        //Since we are using the +-2G tolerace, we multiply the data by .061 and divide by 100
        data -> accel_x = (((buffer[0] << 8) | buffer[1]) * .061) / 100;
        data -> accel_y = (((buffer[2] << 8) | buffer[3]) * .061) / 100;
        data -> accel_z = (((buffer[4] << 8) | buffer[5]) * .061) / 100;
        
        //The calculations below are found on the arduino forms:
        //https://forum.arduino.cc/t/converting-raw-data-from-mpu-6050-to-yaw-pitch-and-roll/465354/18
        //calculate pitch by taking the arctan of z and x, multiply by 180 and divide by pi
        pitch = (atan2(data->accel_z, data->accel_x) * 180) / M_PI;
        
        //calculate roll by taking the arctan of z and y, multiply by 180 and divide by pi
        roll = (atan2(data->accel_z, data->accel_y) * 180) / M_PI;

        //calculate yaw by taking arctan of the square root of y^2 + z^2 and x, the multiply by 180 and divide by pi
        yaw = (atan2(sqrt(data->accel_y * data->accel_y + data->accel_z * data->accel_z), data->accel_x) * 180) / M_PI; 

        ESP_LOGI(TAG, "accel_x:\t%f accel_y:\t%f accel_z:\t%f", data->accel_x, data->accel_y, data->accel_z);
        
        vTaskDelay(100/portTICK_PERIOD_MS);
		return;    
}

//funtion to handle wifi event
static void wifiHandler(void *arg, esp_event_base_t eventBase, int32_t eventID, void *eventData){
    //function variables
    char *TAG = "WIFIHANDLER";
    const uint8_t MAXTRIES = 10;
    //connect to the access point
    if (eventBase == WIFI_EVENT && eventID == WIFI_EVENT_STA_START){
        ESP_LOGI(TAG, "***CONNECTING TO ACCESS POINT***\n");
        esp_wifi_connect();
    }
    else if (eventBase == WIFI_EVENT && eventID == WIFI_EVENT_STA_DISCONNECTED){
        if (cntTries < MAXTRIES){
            ++cntTries;
            ESP_LOGI(TAG, "***ATTEMPING TO RECONNECT: ATTEMPT %u OUT OF %u***\n", cntTries, MAXTRIES);
            esp_wifi_connect();
        }
        else{
            xEventGroupSetBits(wifiEventLoop, WIFI_FAILURE);
        }
    }
}

//function to handle ip event
static void ipHandler(void *arg, esp_event_base_t eventBase, int32_t eventID, void *eventData){
    //function variables
    char *TAG  = "IPHANDLER";
    ip_event_got_ip_t *event;

    //get IP
    if (eventBase == IP_EVENT && eventID == IP_EVENT_STA_GOT_IP){
        event = (ip_event_got_ip_t*) eventData;
        ESP_LOGI(TAG, "***GOT IP: " IPSTR, IP2STR(&event->ip_info.ip)); 
        cntTries = 0;
        xEventGroupSetBits(wifiEventLoop, WIFI_SUCCESS);
    }
}


//Connect to wifi
esp_err_t wifiConnect(){
    //method variables
    char *TAG = "WIFICONNECT";
    uint8_t status = WIFI_FAILURE;
    wifi_init_config_t wifiCFG = WIFI_INIT_CONFIG_DEFAULT();
    esp_event_handler_instance_t wifiEventHandler;
    esp_event_handler_instance_t ipEventHandler;
    wifi_config_t wifiSettings = {
        .sta = {
            .ssid = SSID,
            .password = PASSWORD,
            .threshold.authmode = WIFI_AUTH_WPA2_PSK,
            .pmf_cfg = {
                .capable = true,
                .required = false
            },
        },
    };
    EventBits_t wifiBits;
    //initialize wifi driver
    ESP_ERROR_CHECK(esp_netif_init());

    //initialize event loop
    ESP_ERROR_CHECK(esp_event_loop_create_default());

    //create station
    esp_netif_create_default_wifi_sta();

    //set station default
    ESP_ERROR_CHECK(esp_wifi_init(&wifiCFG));

    //set up event loop
    wifiEventLoop = xEventGroupCreate();
    ESP_ERROR_CHECK(esp_event_handler_instance_register(WIFI_EVENT, 
                                                        ESP_EVENT_ANY_ID, 
                                                        &wifiHandler, 
                                                        NULL,
                                                        &wifiEventHandler));
    ESP_ERROR_CHECK(esp_event_handler_instance_register(IP_EVENT,
                                                        IP_EVENT_STA_GOT_IP,
                                                        &ipHandler,
                                                        NULL,
                                                        &ipEventHandler));


    //set the wifi mode
    ESP_ERROR_CHECK(esp_wifi_set_mode(WIFI_MODE_STA));

    //set wifi config
    ESP_ERROR_CHECK(esp_wifi_set_config(WIFI_IF_STA, &wifiSettings));

    //start driver
    ESP_ERROR_CHECK(esp_wifi_start());
    ESP_LOGI(TAG, "***WIFI INITIALIZING***\n");

    //block this event until we get success or failure
    wifiBits = xEventGroupWaitBits(wifiEventLoop, 
                                    WIFI_SUCCESS | WIFI_FAILURE,
                                    pdFALSE,
                                    pdFALSE,
                                    portMAX_DELAY);

    //Once we get either success or failure, the proram will stop blocking and this function can check what we got
    if (wifiBits == WIFI_SUCCESS){
        ESP_LOGI(TAG, "***SUCCESSFULLY CONNECTED TO ACCESS POINT***\n");
        status = WIFI_SUCCESS;
    }
    else if (wifiBits == WIFI_FAILURE){
        ESP_LOGE(TAG, "***FAILED TO CONNECT TO ACCESS POINT***\n");
        status = WIFI_FAILURE;
    }
    else{
        ESP_LOGE(TAG, "***UNEXPECTED EVENT***\n");
        status = WIFI_FAILURE;
    }
    
    //unregister the events
    ESP_ERROR_CHECK(esp_event_handler_instance_unregister(WIFI_EVENT, ESP_EVENT_ANY_ID, wifiEventHandler));
    ESP_ERROR_CHECK(esp_event_handler_instance_unregister(IP_EVENT, IP_EVENT_STA_GOT_IP, ipEventHandler)); 
    vEventGroupDelete(wifiEventLoop);

    return status;
}


int tcpConnect (){
    //function variables
    const char *TAG = "TCPCONNECT";
	int sockFD = 0;
    const int PORT = 8080;
    const int MAX_CONNECTIONS = 10;
    int n = 0;
    char buffer[255] = {' '};
    struct sockaddr_in serverAddr;
    struct sockaddr_in clientAddr;
    socklen_t cliLen;
	data_t data = {
        .accel_x = 0.0,
        .accel_y = 0.0,
        .accel_z = 0.0
    };

    //create the socket
    sockFD = socket(AF_INET, SOCK_STREAM, 0);
    if (sockFD < 0){
        ESP_LOGE(TAG, "***COULD NOT OPEN SOCKET***\n");
    }
    else{
        ESP_LOGI(TAG, "***SOCKET OPENED***\n");
    }
    
    //clear the server address
    memset(&serverAddr, 0, sizeof(serverAddr));

    //configure the socket
    serverAddr.sin_family = AF_INET;
    serverAddr.sin_addr.s_addr = INADDR_ANY;
    serverAddr.sin_port = htons(PORT);
     
    
    //bind the port to the address
    if (bind(sockFD, (struct sockaddr *) &serverAddr, sizeof(serverAddr)) < 0){
        ESP_LOGE(TAG, "***COULD NOT BIND TO PORT***\n");
    }
    else{
        ESP_LOGI(TAG, "***PORT BINDED***\n");
    }
  
    //listen for incoming connections
    ESP_LOGI(TAG, "***LISTENING...***\n");
    listen(sockFD, MAX_CONNECTIONS);
    cliLen = sizeof(clientAddr);
    
    //accept the connections
    sockFD = accept(sockFD, (struct sockaddr *) &clientAddr, &cliLen);
    if (sockFD < 0){
        ESP_LOGE(TAG, "***COULD NOT ACCEPT THE SOCKET***\n");
    }
    else{
        printf("***SOCKET ACCEPTED***\n");
    }
   
   
    while(true){
        //clear the buffer to ensure there is no data left over
        memset(&buffer, 0, sizeof(buffer));

        //read data from the client
        n = read(sockFD, buffer, sizeof(buffer));
        if (n < 0){
            ESP_LOGE(TAG, "***COULD NOT READ FROM THE CLIENT***\n");
        }
        ESP_LOGI(TAG, "CLIENT: %s\n", buffer);
        
        if (strcmp("QUIT\n", buffer) == 0){	
        	ESP_LOGI(TAG, "***We are waiting***\n");
			strcpy(buffer, "WAITING\n");
            n = write(sockFD, buffer, strlen(buffer));	
			if (n < 0){
    	        ESP_LOGE(TAG, "***COULD NOT WRITE TO THE CLIENT***\n");
    	    }
        }
		else if (strcmp("START\n", buffer) == 0){		
        	ESP_LOGI(TAG, "***We are sending***\n");
			i2cMasterInit(&data);

        	//clear the buffer again
        	memset(&buffer, 0, sizeof(buffer));
	
			//copy the data from the i2cMasterInit to the buffer
			sprintf(buffer, "%f\n", (data.accel_z - 9.81));

    	    //send the data to the client
		   	n = write(sockFD, buffer, strlen(buffer));	
			if (n < 0){
    	        ESP_LOGE(TAG, "***COULD NOT WRITE TO THE CLIENT***\n");
    	    }
		}
    }
    shutdown(sockFD, 0);
    close(sockFD);
    exit(EXIT_SUCCESS);
}
/*
//connect to tcp server
int tcpConnect(void){
    //function variables
    int sockFD = socket(AF_INET, SOCK_STREAM, 0);
    int err = 0;
    int n = 0;
    const int PORT = 8080;
    const char *HOSTNAME = "192.168.51.246";
    const char *TAG = "TCPCONNECT";
    char buffer[256] = ""; 
    struct hostent *host;
    struct sockaddr_in serverAddress;
    //create a socket
    if (sockFD < 0){
        ESP_LOGE(TAG, "***UNABLE TO CREATE A SOCKET: %d***\n", errno);
        return TCP_FAILURE;
    }
    //setup host parameters
    host = gethostbyname(HOSTNAME);
    if (host == NULL){
       ESP_LOGE(TAG, "***UNABLE TO FIND HOSTNAME***\n");
       return TCP_FAILURE;
    }
       
    //config the host parameter
    serverAddress.sin_family = AF_INET;
    serverAddress.sin_port = htons(PORT);
    serverAddress.sin_addr.s_addr = *(in_addr_t *) host -> h_addr;

    //connect to the server
    err = connect(sockFD, (struct sockaddr *) &serverAddress, sizeof(serverAddress));
    if (err != 0){
    ESP_LOGE(TAG, "***UNABLE TO CONNECT TO SERVER: %d***\n", errno);
        perror("***UNABLE TO CONNECT TO SERVER***\n");
        return TCP_FAILURE;
    }
    else{
        ESP_LOGI(TAG, "***CONNECTED TO THE SERVER***\n");
    }

    //tell the server you want to talk
    strcpy(buffer, "OK");
    n = write(sockFD, buffer, sizeof(buffer));
    if (n < 0){
        ESP_LOGE(TAG, "***FAILED TO WRITE TO SERVER\n***");
        return TCP_FAILURE;
    }
    while(true){
        //clear the buffer to ensure there is no data left over
        memset(&buffer, 0, sizeof(buffer));

        //get response from server
        n = read(sockFD, buffer, sizeof(buffer));
        if (n < 0){
            ESP_LOGE(TAG, "***COULD NOT READ FROM SERVER\n***");
            return TCP_FAILURE;
        }
        ESP_LOGI(TAG, "***SERVER: %s\n***", buffer);
        
        if (strcmp("QUIT", buffer) == 0){
            memset(buffer, 0, sizeof(buffer));
            strcpy("CONFIRMED", buffer);
            n = write(sockFD, buffer, sizeof(buffer));
        }
        else { 
            memset(buffer, 0, sizeof(buffer));
            strcpy("NOT CONFIRMED", buffer);
            n = write(sockFD, buffer, sizeof(buffer));
        }
    }
    shutdown(sockFD, 0);
    close(sockFD);
    return TCP_SUCCESS;
}
*/
//main
void app_main(void){
    const char *TAG = "MAIN";
	
    //flash the nvs
    esp_err_t status = nvs_flash_init();
    if (status != ESP_OK){
        ESP_LOGE(TAG, "***COULD NOT INITIALIZE NVS***");
        nvs_flash_erase();
        ESP_ERROR_CHECK(nvs_flash_init());
    }

    //connect to the access point
    status = wifiConnect();
    if (status != WIFI_SUCCESS){
        ESP_LOGE(TAG, "***COULD NOT CONNECT TO AP***\n");
        exit(EXIT_FAILURE);
    }

    status = tcpConnect();
    if (status != TCP_SUCCESS){
        ESP_LOGI(TAG, "***IT DIDNT WORK***\n");
    }

    //if we don't set a delay the esp will crash
    vTaskDelay(1000 / portTICK_PERIOD_MS);
}
