FROM ubuntu:latest
LABEL authors="hugopaixao"

ENTRYPOINT ["top", "-b"]