#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <stdbool.h>

//write an error function
void error (const char *msg){
    perror(msg);
    exit(EXIT_FAILURE);
}

int main (int argc, char *argv[]){
    //function variables
    int sockFD = 0;
    int PORT = 0;
    const int MAX_CONNECTIONS;
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
    else{
        printf("***SOCKET OPENED***\n");
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
    else{
        printf("***PORT BINDED***\n");
    }
    
    //listen for incoming connections
    listen(sockFD, MAX_CONNECTIONS);
    cliLen = sizeof(clientAddr);
    
    //accept the connections
    sockFD = accept(sockFD, (struct sockaddr *) &clientAddr, &cliLen);
    if (sockFD < 0){
        error("***COULD NOT ACCEPT THE SOCKET***\n");
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
            error("***COULD NOT READ FROM THE CLIENT***\n");
        }
        printf("CLIENT: %s\n", buffer);

        //clear the buffer again
        memset(&buffer, 0, sizeof(buffer));

        //read input from the server (essentially a safer version of scanf)
        fgets(buffer, sizeof(buffer), stdin);

        //send the data to the client
        n = write(sockFD, buffer, strlen(buffer));
        if (n < 0){
            error("***COULD NOT WRITE TO THE CLIENT***\n");
        }

        if (strcmp("QUIT", buffer) == 0){
            break;
        }

        close(sockFD);
    }

    exit(EXIT_SUCCESS);
}
