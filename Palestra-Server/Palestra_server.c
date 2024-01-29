#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>

//write an error function
void error (const char *msg){
    perror(msg);
    exit(EXIT_FAILURE);
}

int main (int argc, char *argv[]){
    //function variables
    int tempSockFD = 0;
    int sockFD = 0;
    int PORT = 0;
    int n = 0;
    char buffer[255] = {' '};
    struct sockaddr_in serverAddr;
    struct sockaddr_in clientAddr;
    socklen_t cliLen;

    //the user should enter a port number as a command line argument
    if (argc < 2){
        printf("***PORT NUMBER NOT PROVIDED***\n");
        exit(EXIT_FAILURE);
    }

    //create the socket
    sockFD = socket(AF_INET, SOCK_STREAM, 0);
    if (sockFD < 0){
        error("***COULD NOT OPEN SOCKET***\n");
    }
    
    //clear the server address
    memset(&serverAddr, 0, sizeof(serverAddr));

    //get the port number
    PORT = atoi(argv[1]);

    //configure the socket
    serverAddr.sin_family = AF_INET;
    serverAddr.sin_addr.s_addr = INADDR_ANY;
    serverAddr.sin_port = htons(PORT);

    //bind the port to the address
    if (bind(sockFD, (struct sockaddr *) &serverAddr, sizeof(serverAddr)) < 0){
        error("***COULD NOT BIND TO PORT***\n");
    }

    exit(EXIT_SUCCESS);
}
