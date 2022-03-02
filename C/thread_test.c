/*
 *  Multi thread program
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <errno.h>
#include <pthread.h>

#define MAX_THREADS 10
#define MAX_CNT 100

int count_1 = 0;
int count_2 = 0;

pthread_mutex_t mutex, mutex_start;
pthread_cond_t cond_start;

void thread(void);

int main(void)
{
    int i = 0;
    pthread_t thread_id[MAX_THREADS];
    void *thread_return;

    pthread_mutex_init(&mutex, NULL);
    pthread_mutex_init(&mutex_start, NULL);
    pthread_cond_init(&cond_start, NULL);
    
    srand(time(NULL));
    /* Create new threads */
    for (i = 0; i < MAX_THREADS; i++) {
         if (pthread_create(&thread_id[i], NULL, (void *)thread, NULL) < 0) {
            perror("pthread_create error");
            exit(1);
         }
    }
    sleep(1);
    pthread_cond_broadcast(&cond_start);

    /* Wait for the end of threads */
    for (i = 0; i < MAX_THREADS; i++) {
        if (pthread_join(thread_id[i], &thread_return) < 0) {
            perror("pthread_join error");
            exit(1);
        }
    }
    pthread_mutex_destroy (&mutex); 

    printf("count_1  = %d\n", count_1);
    printf("count_2  = %d\n", count_2);
    return 0;
}

/**
 * Thread
 */
void thread(void)
{
    int i;
    int cnt;

    pthread_mutex_lock(&mutex_start);
    pthread_cond_wait(&cond_start, &mutex_start);
    pthread_mutex_unlock(&mutex_start);
    printf("thread [%lu] start\n", pthread_self());
    if (rand() < RAND_MAX / 2) {
        usleep(1000);
    }
    for (i = 0; i < MAX_CNT; i++) {
        cnt = count_2;
        cnt++;
        count_2 = cnt;
        usleep(2000);
    }
    for (i = 0; i < MAX_CNT; i++) {
        pthread_mutex_lock(&mutex);
        cnt = count_1;
        cnt++;
        count_1 = cnt;
        pthread_mutex_unlock(&mutex);
    }
    printf("thread [%lu] end\n", pthread_self());
    pthread_exit(0);
}
