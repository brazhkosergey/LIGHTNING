Класс src.main.Main.java запускает приложение. При этом проверяя количество оперативной памяти выделенное для Java приложений.
В случае если памяти меньше 25000 мб., программа будет перезапущена автоматически, с выделением памяти в размере 29000 мб.


Папки: Программа будет хранить файлы буфера и текстовый файл с данными в папке C:\LIGHTNING_STABLE\. Папку, в которую программа будет сохранять байты с потока, так же видео записи, созданные с этих байт можно указать в настройка. В случае если не указана другая папка, вся информация будет храниться в папке по умолчанию. 

На главном окне располагаются восемь маленьких окон (CameraPanel.class).

Для того что бы  сохранить запись с видео камер в программе используются следующие классы: 
•	VideoCatcher.class – класс отвечает за считывание данных с видеопотока с одной камеры. В каждом экземпляре класса запущено три потока. Первый считывает данные с потока, отделяет байты одного изображения, складывает байты в массив, создает привязку к точному времени (в миллисекундах), отправляет эти данные в VideoCreator.class и увеличивает счетчик кадров. Второй поток ежесекундно обнуляет счетчик кадров, выводит данные о FPS на экран, и следит за подключением камеры. В случае перебоев с интернетом, будет пробовать перезапускать подключение. Третий поток обновляет картинку на экран, обновляет данные о параметрах прозрачности фона, количества белого и т.д. 
•	VideoCreator.class – класс отвечает за сохранение данных с видео потоков двух камер. В программе 4 экземпляра этого класса. По экземпляру на две смежные камеры. VideoCreator принимает массивы байтов в привязкой к времени от двух камер (или одной, если включена одна камера). Эти данные складываются в коллекцию (Точная дата (как указатель) – массив байт (как объект)). Точные даты складываются в очередь из дат. В каждом экземпляре класса запущено два потока. Первый – каждую миллисекунду проверяет нужно ли сохранить файл. Если флажок сохранения включен, поток фиксирует количество элементов в очереди с датами. И в отдельном потоке запускает сохранение данных на диск, программа по указателям дат из очереди берет массивы байт с коллекций, и сохраняет в файл (в файле данные полученные с двух камер за одну секунду). Ссылку на этот файл храним в отдельной очереди файлов, в имени файла информация о количестве кадров в файле, времени создания. После сохранения файла. В случае если было событие, поток проверяет, сколько секунд уже сохранено. Если достаточно, сохраняет все файлы с очереди файлов, в папку для данных, указанную в настройках. Если секунд не достаточно, поток заканчивает работу. В случае если событий не было, поток проверяет размер очереди файлов, если в очереди больше файлов (секунд), чем указано в настройках, удаляется первый файл, и ссылку на него. Второй поток – таймер. Каждую секунду переключает флажок, который сигнализирует запись секунды в файл. В случае наличия события, увеличивает счетчик времени, которое уже сохранено. 

