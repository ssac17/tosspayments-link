FROM ubuntu:latest
LABEL authors="sky"

ENTRYPOINT ["top", "-b"]