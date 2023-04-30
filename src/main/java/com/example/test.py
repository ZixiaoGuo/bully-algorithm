import matplotlib.pyplot as plt

x_values = [5, 10, 18, 30, 60, 90, 120, 150]

y1_values = [6, 16, 14, 24, 54, 84, 114, 144]
y2_values = [6, 16, 10, 19, 49, 79, 109, 139]
y3_values = [6, 16, 14, 14, 44, 74, 104, 134]

plt.plot(x_values, y1_values, label='Group size = 5 (Re-election)', marker='o')
plt.plot(x_values, y2_values, label='Group size = 10 (Re-election)', marker='o')
plt.plot(x_values, y3_values, label='Group size = 15 (Re-election)', marker='o')

plt.xlabel('Number of nodes', fontsize=20)
plt.ylabel('Messages passed', fontsize=20)
plt.title('Initial election cost', fontsize=20)

for x, y1, y2, y3 in zip(x_values, y1_values, y2_values, y3_values):
    if y1 is not None:
        plt.text(x, y1, str(y1), fontsize=20, verticalalignment='bottom', horizontalalignment='right')
    plt.text(x, y2, str(y2), fontsize=20, verticalalignment='bottom', horizontalalignment='right')
    plt.text(x, y3, str(y3), fontsize=20, verticalalignment='bottom', horizontalalignment='right')


plt.ylim(0, 170)
plt.legend(fontsize=20)
plt.show()