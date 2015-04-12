
file = 'oddsMatrix'
extn = '.csv'
c = {'0' '1' '2'}

for i = 0:9
    file_name = strcat(file, num2str(i), extn)
     v = load(file_name);
     v_mat_c1 = reshape(v(:,1), [28, 28]);
     v_mat_c2 = reshape(v(:,2), [28, 28]);
     v_mat_odds = reshape(v(:,3), [28, 28]);

     v_mat_c1 = v_mat_c1';
     v_mat_c2 = v_mat_c2';
     v_mat_odds = v_mat_odds';

     h = figure
     subplot(1,3,1)
     
     image(v_mat_c1,'CDataMapping','scaled')
     title(strcat('c1Test',num2str(i)))

     subplot(1,3,2)
     image(v_mat_c2,'CDataMapping','scaled')
     title(strcat('c2Test',num2str(i)))

     
     subplot(1,3,3)
     image(v_mat_odds,'CDataMapping','scaled')
     title(strcat('oddsTest',num2str(i)))
     
     saveas(h, strcat('fig', num2str(i)),'jpg');
    
end
