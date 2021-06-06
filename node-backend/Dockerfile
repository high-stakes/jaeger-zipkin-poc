FROM node:14.16 as build

WORKDIR /usr/src/app

COPY package*.json ./
RUN npm install
COPY src/ src
COPY proto/ ../proto
RUN ls -la ..
RUN npm run build

FROM node:14.16

COPY --from=build /usr/src /usr/src
WORKDIR /usr/src/app

ENTRYPOINT ["npm"]
CMD [ "run", "start" ]