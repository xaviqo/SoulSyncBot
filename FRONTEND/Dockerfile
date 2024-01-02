FROM node:14 as builder

WORKDIR /app
COPY . .

RUN npm install
RUN npm run build

# Etapa final con servidor web ligero
FROM node:14-alpine

WORKDIR /app
COPY --from=builder /app/dist /app

RUN npm install -g serve

EXPOSE 7316
CMD ["serve", "-s", ".", "-p", "7316"]
