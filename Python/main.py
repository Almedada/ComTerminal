import serial
import time

# Функция для отправки данных через Bluetooth


def send_data(ser, data):
    """
    Отправляет данные через Bluetooth.
    """
    try:
        # Добавление буквы 'r' к данным перед отправкой
        data_with_r = data + 'r'
        ser.write(data_with_r.encode("utf-8"))  # Отправляем данные как байты
        print(f"Данные отправлены: {data_with_r}")
    except serial.SerialException as e:
        print(f"Ошибка при отправке данных: {e}")

# Функция для прослушивания входящих данных и их отправки обратно


def listen_for_data_and_send(ser):
    """
    Слушает данные и отправляет их.
    """
    while True:
        try:
            if ser.in_waiting > 0:
                data = ser.read(ser.in_waiting)  # Чтение доступных данных
                received_data = data.decode('utf-8')
                print(f"Получены данные: {received_data}")

                # Пример отправки данных обратно с добавлением 'r'
                send_data(ser, "Ответ: " + received_data)

            time.sleep(1)
        except serial.SerialException as e:
            print(f"Ошибка при получении данных: {e}")
            break
        except KeyboardInterrupt:
            print("Ожидание прервано пользователем.")
            break

# Подключение к Bluetooth-устройству через COM-порт


def connect_bluetooth(port):
    """
    Подключение к Bluetooth через COM-порт.
    """
    try:
        # Укажите COM-порт для Bluetooth-соединения
        ser = serial.Serial(port, baudrate=9600, timeout=1)
        print(f"Подключение к {port} успешно.")
        return ser
    except serial.SerialException as e:
        print(f"Ошибка при подключении: {e}")
        return None


# Основной код
if __name__ == "__main__":
    # Укажите правильный порт для вашего Bluetooth-устройства,
    # например, "COM3" или "/dev/rfcomm0"
    port = "COM3"
    ser = connect_bluetooth(port)

    if ser:
        # Запуск прослушивания и отправки данных
        listen_for_data_and_send(ser)
