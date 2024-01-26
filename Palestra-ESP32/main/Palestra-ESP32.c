/*******************************************
Author: Michael Giardina
Purpose: This program will:
            1) Read in data from an MPU6050 
            2) Convert the raw data to useful
                data points
            3) Send that data to an AWS 
Todo: 1) Add wifi component
*******************************************/
//includes
#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <math.h>
#include <string.h>
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
#define SSID "Verizon-SM-J727V-0484"
#define PASSWORD "ivfe307#"

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
void i2cMasterInit(void *ignore){
    //variables
    const char *TAG = "i2cMasterInit";
    uint8_t buffer[14] = {0};
    data_t data = {
        .accel_x = 0.0,
        .accel_y = 0.0,
        .accel_z = 0.0
    };
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
    
    //install the driver
    ESP_ERROR_CHECK(i2c_driver_install(I2C_NUM_0, I2C_MODE_MASTER, 0, 0, 0));
    
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
    
    while(true){
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
        data.accel_x = (((buffer[0] << 8) | buffer[1]) * .061) / 100;
        data.accel_y = (((buffer[2] << 8) | buffer[3]) * .061) / 100;
        data.accel_z = (((buffer[4] << 8) | buffer[5]) * .061) / 100;
        
        //The calculations below are found on the arduino forms:
        //https://forum.arduino.cc/t/converting-raw-data-from-mpu-6050-to-yaw-pitch-and-roll/465354/18
        //calculate pitch by taking the arctan of z and x, multiply by 180 and divide by pi
        pitch = (atan2(data.accel_z, data.accel_x) * 180) / M_PI;
        
        //calculate roll by taking the arctan of z and y, multiply by 180 and divide by pi
        roll = (atan2(data.accel_z, data.accel_y) * 180) / M_PI;

        //calculate yaw by taking arctan of the square root of y^2 + z^2 and x, the multiply by 180 and divide by pi
        yaw = (atan2(sqrt(data.accel_y * data.accel_y + data.accel_z * data.accel_z), data.accel_x) * 180) / M_PI; 

        ESP_LOGI(TAG, "pitch:\t%f accel_y:\t%f accel_z:\t%f", data.accel_x, data.accel_y, data.accel_z);
        
        vTaskDelay(1000/portTICK_PERIOD_MS);
    }
    vTaskDelete(NULL);
}

//Connect to wifi
esp_err_t wifi_connect(){
    //method variables
    uint8_t status = WIFI_FAILURE;
    wifi_init_congif_t wifiCFG = WIFI_INIT_CONFIG_DEFAULT();
    static EventGroupHandle_t wifiEventLoop;
    esp_event_handler_instance_t wifiEventHandler;
    esp_event_handler_instance_t ipEventHandler;
    wifi_config_t wifiSettings = {
        .sta = {
            .ssid = SSID,
            .password = PASSWORD,
            .threshold.authmode = WIFI_AUTH_WPA2_P2K,
            .pmf_cfg = {
                .capable = true,
                .required = false
            },
        },
    };
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
                                                        IP_STA_GOT_IP,
                                                        &ipHandler,
                                                        NULL,
                                                        &ipEventHandler));


    //set the wifi mode
    ESP_ERROR_CHECK(esp_wifi_mode(WIFI_MODE_STA));

    //set wifi config
    ESP_ERROR_CHECK(esp_wifi_set_config(WIFI_IF_STA, &wifiSettings);




}

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
    status = wifi_connect();
    if (status != ESP_OK){
        ESP_LOGE(TAG, "***COULD NOT CONNECT TO AP***\n");
        exit(EXIT_FAILURE);
    }

    //create a task to start reading the MPU6050
    xTaskCreate(i2cMasterInit, "INITMASTER", 1024, NULL, 1, NULL);
}
